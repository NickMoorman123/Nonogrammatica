package application;

public class PicrossSolverSquare {
	private Boolean couldBeFilled;
	private Boolean couldBeEmpty;
	
	//we can go from true to false but not false to true
	public void alreadyFilled() {
		couldBeFilled = true;
		couldBeEmpty = false;
	}
	
	public void alreadyEmpty() {
		couldBeFilled = false;
		couldBeEmpty = true;
	}
	
	public void mayBeFilled() throws Exception {
		if (Boolean.FALSE.equals(couldBeFilled)) {
			throw new Exception();
		} else {
			couldBeFilled = true;
		}
	}
	
	public void mayBeEmpty() throws Exception {
		if (Boolean.FALSE.equals(couldBeEmpty)) {
			throw new Exception();
		} else {
			couldBeEmpty = true;
		}
	}
	
	public void cantBeFilled() {
		couldBeFilled = false;
	}
	
	public void cantBeEmpty() {
		couldBeEmpty = false;
	}
	
	public Boolean getFilled() {
		return couldBeFilled;
	}
	
	public Boolean getEmpty() {
		return couldBeEmpty;
	}
}