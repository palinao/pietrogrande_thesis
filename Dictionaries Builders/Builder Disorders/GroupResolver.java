import java.io.*;
import java.util.*;

public class GroupResolver{
	public static void main(String[] args){
		try{
			Scanner s=new Scanner(new File("FirstEntries.tsv"));
			PrintWriter writer=new PrintWriter("Disorders_UMLS.tsv", "UTF-8");
			String previous=null;
			while(s.hasNextLine()){
				String line=s.nextLine();
				String id=line.substring(5,13);
				if(!id.equals(previous)) writer.println(line);
				previous=id;
			}
			writer.close();
			s.close();
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
}