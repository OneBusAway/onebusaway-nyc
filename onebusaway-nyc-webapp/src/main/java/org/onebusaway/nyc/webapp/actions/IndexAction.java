/**
 * Copyright 2010, OpenPlans Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onebusaway.nyc.webapp.actions;

import org.onebusaway.nyc.webapp.actions.OneBusAwayNYCActionSupport;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

/**
 * Action for home page
 * 
 */
public class IndexAction extends OneBusAwayNYCActionSupport {

  private static final long serialVersionUID = 1L;
  
  @Override
	public String execute() throws Exception {
	    ActionContext context = ActionContext.getContext();
	    ActionInvocation invocation = context.getActionInvocation();
	    ActionProxy proxy = invocation.getProxy();

	    String name = proxy.getActionName().toLowerCase();

	    // FIXME: since Struts doesn't seem to like wildcard namespaces (in wiki/IndexAction) and default
	    // actions, we have to have this action check to see if it's being called as a "default" action and
	    // return the 404 message if so. There has to be a better way than this? 
	    if(! name.equals("") && ! name.equals("index"))
	    	return "NotFound";
	    
	    return SUCCESS;
  }

}
