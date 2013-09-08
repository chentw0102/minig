package fr.aliasource.webmail.client.shared;

import java.io.Serializable;
import java.util.Date;

public class VacationInfo implements Serializable {

	private static final long serialVersionUID = 9185663313352641309L;

	private boolean enabled;
	private Date start;
	private Date end;
	private String text;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
