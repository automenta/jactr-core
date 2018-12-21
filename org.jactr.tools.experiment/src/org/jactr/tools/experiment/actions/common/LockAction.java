package org.jactr.tools.experiment.actions.common;

/*
 * default logging
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.tools.experiment.IExperiment;
import org.jactr.tools.experiment.actions.IAction;
import org.jactr.tools.experiment.impl.IVariableContext;

public class LockAction implements IAction
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LockAction.class);
  
  private IExperiment _experiment;
  private String _name;
  
  public LockAction(String lockName, IExperiment experiment)
  {
    _experiment = experiment;
    _name = lockName;
  }

  public void fire(IVariableContext context)
  {
    String lock = _experiment.getVariableResolver().resolve(_name, context).toString();
   _experiment.getLockManager().lock(lock); 
  }

}