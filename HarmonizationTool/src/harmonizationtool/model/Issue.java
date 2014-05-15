package harmonizationtool.model;


public class Issue {
  private static String issue;
  private static String location;
  private static String details;
  private static String suggestion;
  private static Status status;
  public void resolveIssue(){
	  if (status == Status.UNRESOLVED){
		  status = Status.RESOLVED;
	  }
  }
}
