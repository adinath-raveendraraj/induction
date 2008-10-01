/**
 *   Copyright 2008 Acciente, LLC
 *
 *   Acciente, LLC licenses this file to you under the
 *   Apache License, Version 2.0 (the "License"); you
 *   may not use this file except in compliance with the
 *   License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in
 *   writing, software distributed under the License is
 *   distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 *   OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing
 *   permissions and limitations under the License.
 */
package com.acciente.commons.loader;

import com.acciente.commons.javac.JavaCompilerManager;

import java.io.File;

/**
 * An class definition loader that compiles and loads Java source files. 
 *
 * @created Feb 27, 2008
 *
 * @author Adinath Raveendra Raj
*/
public class JavaSourceClassDefLoader implements ClassDefLoader
{
   private File                  _oSourceDirectory;
   private String                _sPackageNamePrefix;
   private JavaCompilerManager   _oJavaCompilerManager;

   public JavaSourceClassDefLoader()
   {
   }

   public JavaSourceClassDefLoader( JavaCompilerManager oJavaCompilerManager, File oSourceDirectory )
   {
      _oJavaCompilerManager   = oJavaCompilerManager;
      _oSourceDirectory       = oSourceDirectory;
   }

   public ClassDef getClassDef( String sClassName ) throws ClassNotFoundException
   {
      if ( sClassName == null )
      {
         throw new IllegalArgumentException( "class definition loader requires a classname, none specified!" );
      }

      if ( _oJavaCompilerManager == null )
      {
         throw new IllegalStateException( "no compiler manager defined, unable to load class from source, class: " + sClassName );
      }

      File oSourceFile = new File( _oSourceDirectory, getFileNameFromClassName( sClassName ) );

      JavaSourceClassDef oClassDef = null;

      if ( oSourceFile.exists() )
      {
         oClassDef = new JavaSourceClassDef( sClassName, oSourceFile, _oJavaCompilerManager );
      }

      return oClassDef;
   }

   public ResourceDef getResourceDef( String sResourceName )
   {
      // not implemented at this time since this class def loader is not currently used/supported
      return null;
   }

   private String getFileNameFromClassName( String sClassName )
   {
      String   sFilename;

      if ( _sPackageNamePrefix == null )
      {
         sFilename = sClassName.replace( '.', '/' ) + ".java";
      }
      else
      {
         // first check if the classname is ok
         if ( ! sClassName.startsWith( _sPackageNamePrefix ) )
         {
            throw new IllegalArgumentException( "class definition loader is restricted to classnames that start with: " + _sPackageNamePrefix + ", classname: " + sClassName );
         }

         if ( sClassName.equals( _sPackageNamePrefix ) )
         {
            throw new IllegalArgumentException( "specified classname is identical to package name prefix!" );
         }

         sFilename = sClassName.substring( _sPackageNamePrefix.length() + 1 ).replace( '.', '/' ) + ".java";
      }

      return sFilename;
   }

   /**
    * Returns the source directory from which this classloader loads java source files
    *
    * @return a File object representing a directory path
    */
   public File getSourceDirectory()
   {
      return _oSourceDirectory;
   }

   /**
    * Sets the directory from which this classloader will attempt to java source
    * files
    *
    * @param sSourceDirectory  a File object representing a directory path
    */
   public void setSourceDirectory( File sSourceDirectory )
   {
      _oSourceDirectory = sSourceDirectory;
   }

   /**
    * Returns the package name prefix set by a call to setPackageNamePrefix() or
    * a null string if no package name prefix was ever set.
    *
    * @return null or a package name without a terminating period (e.g: foo.bar and NOT foo.bar.)
    */
   public String getPackageNamePrefix()
   {
      return _sPackageNamePrefix;
   }

   /**
    * A package name prefix that represents the prefix that should be removed from
    * the class name passed to loadClass() before attempting to convert the class's
    * package name to a sub-directory off the specified source directory.
    *
    * For consistency this setting also causes the class loader to enfore that all
    * the package name of classes loaded by this classloader start with the specified
    * package name prefix.
    *
    * Following is an illustration of how is parameter may be used. Assume we have
    * source files in a directory /project/foobar/src, lets also assume that we want to
    * reliably restrict this class loader to only load source files in the folder
    * /project/foobar/src/foobar/controller. So for so security we set the source
    * directory of this classoader to /project/foobar/src/foobar/controller.
    *
    * Now if we ask this classloader to load the class foobar.controller.HelloWorld
    * it would (unsuccessfully) try to look for a source file named:
    * "/project/foobar/src/foobar/controller/foobar/controller/HelloWorld.java"
    * computed using:
    * SourceDir="/project/foobar/src/foobar/controller"
    * + InferredSourceFilePath="/foobar/controller/HelloWorld.java"
    *
    * To get the correct behaviour in the above example the PackageNamePrefix of this
    * class loader must be set to "foobar.controller". The prefix "foobar.controller"
    * is then removed from the package name of class before using it to compute the
    * source file path for the class foobar.controller.HelloWorld.
    *
    * @param sPackageNamePrefix null or a package name without a terminating period (e.g: foo.bar and NOT foo.bar.)
    */
   public void setPackageNamePrefix( String sPackageNamePrefix )
   {
      if ( sPackageNamePrefix != null && sPackageNamePrefix.endsWith( "." ) )
      {
         throw new IllegalArgumentException( "the package name prefix should be not be terminated by a period" );
      }
      _sPackageNamePrefix = sPackageNamePrefix;
   }

   public JavaCompilerManager getJavaCompilerManager()
   {
      return _oJavaCompilerManager;
   }

   public void setJavaCompilerManager( JavaCompilerManager oJavaCompilerManager )
   {
      _oJavaCompilerManager = oJavaCompilerManager;
   }
}

// EOF