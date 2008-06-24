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
package com.acciente.induction.dispatcher;

/**
 * Internal.
 *
 * @created Apr 14, 2008
 *
 * @author Adinath Raveendra Raj
 */
public class ParamResolverException extends Exception
{
   public ParamResolverException( String sMessage )
   {
      super( sMessage );
   }

   public ParamResolverException( String sMessage, Throwable oCause )
   {
      super( sMessage, oCause );
   }
}

// EOF