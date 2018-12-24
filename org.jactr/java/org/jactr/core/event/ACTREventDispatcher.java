/*
 * Created on Oct 11, 2006 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.core.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Stream;

/**
 * class that handles the nitty gritty of tracking listeners, executors, and
 * propogating that the firing events correctly
 *
 * @author developer
 */
public class ACTREventDispatcher<S, L> {

    static public final Log LOGGER = LogFactory.getLog(ACTREventDispatcher.class);

    private final List<Pair> listeners = new CopyOnWriteArrayList<>();

    private boolean _haveEncounteredREE = false;

    public ACTREventDispatcher() {

    }

    public void clear() {
        listeners.clear();
    }

    public void addListener(L listener, Executor executor) {
        if (listener == null)
            throw new IllegalArgumentException("Listener must not be null");

        if (executor == null) {
            listeners.add(new Pair(listener) {
                @Override
                public void fire(IACTREvent<S, L> event) {
                    execute(event, _listener);
                }
            });
        } else {
            listeners.add(new Pair(listener) {
                @Override
                public void fire(IACTREvent<S, L> event) {
                    try {
                        executor.execute(() -> execute(event, _listener));
                    } catch (RejectedExecutionException ree) {

                        if (!_haveEncounteredREE) {
                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn(String.format(
                                        "%s rejected processing of actr event (%s) by listener (%s).",
                                        executor, event.getClass().getSimpleName(),
                                        _listener.getClass().getName()));
                                LOGGER.warn(
                                        "This is normal during end-of-run processing if your model produced data faster than it could be processed,");
                                LOGGER.warn(
                                        "by the IDE or background tools. If you see this warning mid-run, something may be wrong, please see the exception.");
                                LOGGER.warn(String.format(
                                        "Suppressing further rejection warnings for this event source (%s).",
                                        event.getSource().getClass().getName()));
                                LOGGER.warn(ree);
                            }

                            _haveEncounteredREE = true;
                        }
                    }
                }

            });
        }
    }

    public void removeListener(L listener) {

        if (!listeners.isEmpty())
            listeners.remove(listener);

    }

    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    public void fire(IACTREvent<S, L> event) {
        for (Pair pair : listeners)
            pair.fire(event);
    }

    static <L> void execute(IACTREvent event, L _listener) {
        try {
            event.fire(_listener);
        } catch (Exception e) {
            LOGGER.error("Uncaught exception during event firing of " + event
                    + " to " + _listener, e);
        }
    }

    abstract private class Pair {


        protected L _listener;

        Pair(L listener) {
            _listener = listener;
        }

//        public boolean hasListener(L listener) {
//            return _listener.equals(listener);
//        }

        abstract public void fire(final IACTREvent<S, L> event);

    }

    /**
     * @return
     */
    public Stream<L> getListeners() {
//        if (_actualListeners == null) return Collections.EMPTY_LIST;
//
//        List<L> listeners = new ArrayList<>(_actualListeners.size());
//        for (Pair pair : _actualListeners)
//            listeners.add(pair._listener);
//
        return listeners.stream().map(x -> x._listener);
    }
}
