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
package net.java.dev.moskito.core.logging;

import java.util.Iterator;
import java.util.List;

import net.anotheria.util.Date;
import net.anotheria.util.IdCodeGenerator;
import net.java.dev.moskito.core.producers.IStats;
import net.java.dev.moskito.core.producers.IStatsProducer;
import net.java.dev.moskito.core.timing.IUpdateable;
import net.java.dev.moskito.core.timing.UpdateTriggerServiceFactory;


/**
 * The DefaultStatsLogger class providing a logging utility for stats default value. The default value of a IStat is the 
 * value from begin of counting (usually start of VM or first request).
 * @author lrosenberg
 *
 */
public class DefaultStatsLogger implements IUpdateable{
	private IStatsProducer target;
	private ILogOutput output;
	private String id;
	private int outputIntervalInSeconds;

	/**
	 * Creates a new DefaultStatsLogger with 60 seconds output interval.
	 * @param aTarget the stat producer to monitor.
	 * @param anOutput output logger.
	 */
	public DefaultStatsLogger(IStatsProducer aTarget, ILogOutput anOutput){
		this(aTarget, anOutput, 60);
	}
	
	/**
	 * Creates a new DefaultStatsLogger.
	 * @param aTarget the stats producer to monitor.
	 * @param anOutput the output logger.
	 * @param anOutputIntervalInSeconds output interval in seconds.
	 */
	public DefaultStatsLogger(IStatsProducer aTarget, ILogOutput anOutput, int anOutputIntervalInSeconds){
		target = aTarget;
		output = anOutput;
		id = IdCodeGenerator.generateCode(10);
		outputIntervalInSeconds = anOutputIntervalInSeconds;
		UpdateTriggerServiceFactory.createUpdateTriggerService().addUpdateable(this, outputIntervalInSeconds);
		output.out("Started default interval logger for "+aTarget.getProducerId()+" / "+id);
	}

	public void update(){
		output.out("===============================================================================");
		output.out("=== SNAPSHOT Interval DEFAULT "+outputIntervalInSeconds+"s, Entity: "+id);
		output.out("=== Timestamp: "+Date.currentDate()+", ServiceId: "+target.getProducerId());
		output.out("===============================================================================");
		
		List<IStats> stats = target.getStats();
		for (Iterator<IStats> it = stats.iterator(); it.hasNext(); ){
			IStats stat = it.next();
			output.out(stat.toStatsString());
		}
		
		output.out("===============================================================================");
		output.out("== END: DEFAULT Interval "+outputIntervalInSeconds+"s update, Entity: "+id);
		output.out("===============================================================================");
		
	}

}
