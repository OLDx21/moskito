package net.anotheria.moskito.webui.journey.api;

import java.io.Serializable;
import java.net.URLEncoder;

/**
 * This bean represent a single traced call in an list view. It is usually used to show single calls in a journey.
 * @author lrosenberg
 *
 */
public class JourneySingleTracedCallAO implements Serializable{
	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = -7395976748592447602L;

	/**
	 * Name of the call. 
	 */
	private String name;
	/**
	 * Timestamp of the call execution.
	 */
	private String date;
	
	/**
	 * Number of executed steps in this call.
	 */
	private int containedSteps;

	/**
	 * Call duration.
	 */
	private long duration;

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
		//fix the negative durations.
		if (this.duration<1)
			this.duration*=-1;
	}

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	
	}

	@SuppressWarnings("deprecation")
	public String getNameEncoded(){
		return URLEncoder.encode(getName());
	}
	public int getContainedSteps() {
		return containedSteps;
	}
	public void setContainedSteps(int containedSteps) {
		this.containedSteps = containedSteps;
	}

	@Override
	public String toString() {
		return "JourneySingleTracedCallAO{" +
				"name='" + name + '\'' +
				", date='" + date + '\'' +
				", containedSteps=" + containedSteps +
				", duration=" + duration +
				'}';
	}
}
