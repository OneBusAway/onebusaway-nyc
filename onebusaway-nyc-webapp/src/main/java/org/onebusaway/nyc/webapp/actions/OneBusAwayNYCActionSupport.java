package org.onebusaway.nyc.webapp.actions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.onebusaway.nyc.presentation.impl.WebappIdParser;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.users.services.logging.UserInteractionLoggingService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

/**
 * Abstract class that is currently being used to hang stub data methods onto
 */
public abstract class OneBusAwayNYCActionSupport extends NextActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  protected CurrentUserService _currentUserService;
  
  protected UserInteractionLoggingService _userInteractionLoggingService;
  
  protected List<Double> makeLatLng(double lat, double lng) {
    return Arrays.asList(new Double[] { lat, lng} );
  }

  protected String parseIdWithoutAgency(String id) {
    return new WebappIdParser().parseIdWithoutAgency(id);
  }

  public boolean isAdminUser() {
	    return _currentUserService.isCurrentUserAdmin();
  }
  
  public boolean isAnonymousUser() {
    return _currentUserService.isCurrentUserAnonymous();
  }

  public void setSession(Map<String, Object> session) {
    _session = session;
  }
  
  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  @Autowired
  public void setUserInteractionLoggingService(
      UserInteractionLoggingService userInteractionLoggingService) {
    _userInteractionLoggingService = userInteractionLoggingService;
  }
  
  protected UserBean getCurrentUser() {
    UserBean user = _currentUserService.getCurrentUser();
    if (user == null)
      user = _currentUserService.getAnonymousUser();
    return user;
  }

  protected void logUserInteraction(Object... objects) {

    Map<String, Object> entry = _userInteractionLoggingService.isInteractionLoggedForCurrentUser();

    if (entry == null)
      return;

    ActionContext context = ActionContext.getContext();
    ActionInvocation invocation = context.getActionInvocation();
    ActionProxy proxy = invocation.getProxy();

    entry.put("interface", "web");
    entry.put("namespace", proxy.getNamespace());
    entry.put("actionName", proxy.getActionName());
    entry.put("method", proxy.getMethod());

    if (objects.length % 2 != 0)
      throw new IllegalStateException("expected an even number of arguments");

    for (int i = 0; i < objects.length; i += 2)
      entry.put(objects[i].toString(), objects[i + 1]);

    _userInteractionLoggingService.logInteraction(entry);
  }
  
}
