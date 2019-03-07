/*
 * $Id$
 * 
 * This file is part of the MoSKito software project
 * that is hosted at http://moskito.dev.java.net.
 * 
 * All MoSKito files are distributed under MIT License:
 * 
 * Copyright (c) 2006 The MoSKito Project Team.
 * 
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), 
 * to deal in the Software without restriction, 
 * including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit 
 * persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice
 * shall be included in all copies 
 * or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY
 * OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */	
package net.anotheria.moskito.core.predefined;

import net.anotheria.moskito.core.stats.DefaultIntervals;
import net.anotheria.moskito.core.stats.Interval;

/**
 * Constants used through moskito.
 * @author another
 *
 */
public class Constants {
	
	/**
	 * Default intervals, used if you don't specify anything special. As for now, the default intervals are one minute, 5 minutes, 15 minutes, one hour and one day.
	 */
	private static final Interval[] DEFAULT_INTERVALS = {
		DefaultIntervals.ONE_MINUTE, 
		DefaultIntervals.FIVE_MINUTES,
		DefaultIntervals.FIFTEEN_MINUTES,
		DefaultIntervals.ONE_HOUR,
		DefaultIntervals.TWELVE_HOURS,
		DefaultIntervals.ONE_DAY,
		//DefaultIntervals.DEF_SNAPSHOT,
	};
	
	/**
	 * Min time value.
	 */
	public static final long MIN_TIME_DEFAULT = Long.MAX_VALUE;
	/**
	 * Max time value.
	 */
	public static final long MAX_TIME_DEFAULT = Long.MIN_VALUE;
	
	/**
	 * Average time value.
	 */
	public static final long AVERAGE_TIME_DEFAULT = -1;
	
	public static final Interval[] getDefaultIntervals(){
		return DEFAULT_INTERVALS.clone();
	}

	/**
	 * All intervals with this prefix are considered as snapshot intervals, meaning that they are not updated automatically.
	 */
	public static final String PREFIX_SNAPSHOT_INTERVAL = "snapshot";




}
