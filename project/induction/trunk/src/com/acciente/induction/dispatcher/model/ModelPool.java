package com.acciente.induction.dispatcher.model;

import com.acciente.induction.init.config.Config;
import com.acciente.induction.util.ObjectFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class manages access to the pool model objects
 *
 * Log
 * Mar 16, 2008 APR  -  created
 */
public class ModelPool
{
   private Config.ModelDefs   _oModelDefs;
   private Map                _oAppScopeModelMap;
   private ModelFactory       _oModelFactory;

   public ModelPool( Config.ModelDefs oModelDefs, ModelFactory oModelFactory )
   {
      _oModelDefs          = oModelDefs;
      _oModelFactory       = oModelFactory;
      _oAppScopeModelMap   = new Hashtable();   // we use a hashtable instead of a HashMap for safe concurrent access
   }

   public Object getModel( String sModelClassName, HttpServletRequest oHttpServletRequest )
      throws Exception
   {
      // first find the model definition object for this model class to determine the scope of this model
      Config.ModelDefs.ModelDef oModelDef = _oModelDefs.getModelDef( sModelClassName );

      if ( oModelDef == null )
      {
         throw new IllegalArgumentException( "model-error: no definition for model class: " + sModelClassName );
      }

      // next see if we already have an object instance
      Object oModel;

      if ( oModelDef.isApplicationScope() )
      {
         oModel = getApplicationScopeModel( oModelDef, oHttpServletRequest );
      }
      else if ( oModelDef.isSessionScope() )
      {
         oModel = getSessionScopeModel( oModelDef, oHttpServletRequest );
      }
      else if ( oModelDef.isRequestScope() )
      {
         oModel = getRequestScopeModel( oModelDef, oHttpServletRequest );
      }
      else
      {
         throw new IllegalArgumentException( "model-error: unknown scope for model class: " + sModelClassName );
      }

      return oModel;
   }

   private Object getApplicationScopeModel( Config.ModelDefs.ModelDef oModelDef, HttpServletRequest oHttpServletRequest )
      throws Exception
   {
      Object   oModel;

      oModel = _oAppScopeModelMap.get( oModelDef.getModelClassName() );

      // if the model has not yet been created, we use double-checked locking to ensure that
      // separate threads do not simultaneously instantiate multiple instances of the model
      // I do not think this code will have the potential incorrectness introduced by the
      // double-checked locking, since the model map only contains fully constructed objects 
      if ( oModel == null )
      {
         synchronized ( oModelDef )
         {
            // check again to see if it is still not null, we may have waited on the lock while some
            // other thread was creating this model
            oModel = _oAppScopeModelMap.get( oModelDef.getModelClassName() );

            // if it is still null then go ahead an create the model
            if ( oModel == null )
            {
               oModel = _oModelFactory.createModel( oModelDef, oHttpServletRequest );

               _oAppScopeModelMap.put( oModelDef.getModelClassName(), oModel );
            }
         }
      }
      else
      {
         if ( _oModelFactory.isModelStale( oModelDef, oModel ) )
         {
            synchronized ( oModelDef )
            {
               Object oPreviousModel = oModel;

               oModel = _oModelFactory.createModel( oModelDef, oHttpServletRequest );

               _oAppScopeModelMap.put( oModelDef.getModelClassName(), oModel );

               ObjectFactory.destroyObject( oPreviousModel );
            }
         }
      }

      return oModel;
   }

   private Object getSessionScopeModel( Config.ModelDefs.ModelDef oModelDef, HttpServletRequest oHttpServletRequest )
      throws Exception
   {
      HttpSession oHttpSession;
      Object      oModel;

      oHttpSession = oHttpServletRequest.getSession( true );

      oModel = oHttpSession.getAttribute( oModelDef.getModelClassName() );

      if ( oModel == null )
      {
         synchronized ( oHttpSession )
         {
            oModel = oHttpSession.getAttribute( oModelDef.getModelClassName() );

            if ( oModel == null )
            {
               oModel = _oModelFactory.createModel( oModelDef, oHttpServletRequest );

               oHttpSession.setAttribute( oModelDef.getModelClassName(), oModel );
            }            
         }
      }
      else
      {
         synchronized ( oModel )
         {
            if ( _oModelFactory.isModelStale( oModelDef, oModel ) )
            {
               Object oPreviousModel = oModel;

               oModel = _oModelFactory.createModel( oModelDef, oHttpServletRequest );

               oHttpSession.setAttribute( oModelDef.getModelClassName(), oModel );

               ObjectFactory.destroyObject( oPreviousModel );
            }
         }
      }

      return oModel;
   }

   private Object getRequestScopeModel( Config.ModelDefs.ModelDef oModelDef, HttpServletRequest oRequest )
      throws Exception
   {
      // a request scope object essentially only lasts for the duration of the method invocation so
      // there is no need to pool a copy of the model instance for reuse
      return _oModelFactory.createModel( oModelDef, oRequest );
   }
}

// EOF