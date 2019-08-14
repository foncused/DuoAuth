package me.foncused.duoauth.lib.foncused;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

public class AuthFilter implements Filter {

	private boolean stopped;

	@Override
	public Result filter(final LogEvent record) {
		try {
			if(record != null) {
				final Message msg = record.getMessage();
				if(msg != null) {
					return this.check(msg.getFormattedMessage().toLowerCase());
				} else {
					return Result.NEUTRAL;
				}
			} else {
				return Result.NEUTRAL;
			}
		} catch(final NullPointerException e) {
			return Result.NEUTRAL;
		}
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final String msg, final Object... arg4) {
		try {
			if(msg == null) {
				return Result.NEUTRAL;
			} else {
				return this.check(msg.toLowerCase());
			}
		} catch(final NullPointerException e) {
			return Result.NEUTRAL;
		}
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object o) {
		return this.check(msg.toLowerCase());
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object o, final Object o1) {
		return this.check(msg.toLowerCase());
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object o, final Object o1, final Object o2) {
		return this.check(msg.toLowerCase());
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object o, final Object o1, final Object o2, final Object o3) {
		return this.check(msg.toLowerCase());
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object o, final Object o1, final Object o2, final Object o3, final Object o4) {
		return this.check(msg.toLowerCase());
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object o, final Object o1, final Object o2, final Object o3, final Object o4, final Object o5) {
		return this.check(msg.toLowerCase());
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object o, final Object o1, final Object o2, final Object o3, final Object o4, final Object o5, final Object o6) {
		return this.check(msg.toLowerCase());
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object o, final Object o1, final Object o2, final Object o3, final Object o4, final Object o5, final Object o6, final Object o7) {
		return this.check(msg.toLowerCase());
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object o, final Object o1, final Object o2, final Object o3, final Object o4, final Object o5, final Object o6, final Object o7, final Object o8) {
		return this.check(msg.toLowerCase());
	}

	@Override
	public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object o, final Object o1, final Object o2, final Object o3, final Object o4, final Object o5, final Object o6, final Object o7, final Object o8, final Object o9) {
		return this.check(msg.toLowerCase());
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final Object msg, final Throwable arg4) {
		return this.check(msg.toString().toLowerCase());
	}

	@Override
	public Result filter(final Logger arg0, final Level arg1, final Marker arg2, final Message msg, final Throwable arg4) {
		return this.check(msg.getFormattedMessage().toLowerCase());
	}

	@Override
	public Result getOnMatch() {
		return Result.NEUTRAL;
	}

	@Override
	public Result getOnMismatch() {
		return Result.NEUTRAL;
	}

	@Override
	public State getState() {
		return State.INITIALIZED;
	}

	@Override
	public void initialize() {}

	@Override
	public void start() {
		this.stopped = false;
	}

	@Override
	public void stop() {
		this.stopped = true;
	}

	@Override
	public boolean isStarted() {
		return (!(this.stopped));
	}

	@Override
	public boolean isStopped() {
		return this.stopped;
	}

	private Result check(final String m) {
		try {
			if(m == null) {
				return Result.NEUTRAL;
			}
			return (!(m.matches("^.*issued server command: /(auth|2fa).*$")))
					? Result.NEUTRAL
					: Result.DENY;
		} catch(final NullPointerException e) {
			return Result.NEUTRAL;
		}
	}

}
