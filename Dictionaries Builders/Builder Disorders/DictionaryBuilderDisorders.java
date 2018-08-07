import java.io.*;
import java.util.*;
import java.net.SocketTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DictionaryBuilderDisorders{
	public static void main(String[] args){
		int i=0;
		int t47=0;
		int t19=0;
		int t20=0;
		int t46=0;
		int t48=0;
		int t49=0;
		int t190=0;
		int t191=0;

		try{
			Scanner initial=new Scanner(new File("Disease_or_Syndrome_T047_DISO.tsv"));
			TreeMap entry=new TreeMap();
			System.out.println("Parsing base dictionary");
			while(initial.hasNextLine()){
				String line=initial.nextLine();
				Scanner s1=new Scanner(line);
				s1.useDelimiter("\t");
				String key=s1.next();
				String value=s1.next();
				entry.put(key,value);
				t47++;
			}
			System.out.println("starting entries "+t47);
			for (final File fileEntry : new File("DISO_files").listFiles()) {
				Scanner file=new Scanner(fileEntry);
				System.out.println("Parsing file "+fileEntry.getName());
				String cui="";
				String tui="";
				while(file.hasNextLine()){
					try{
						String line=file.nextLine(); 
						file.nextLine(); //empty line
						cui=line.substring(13,21); //identifier
						tui=line.substring(40,44); //semantic type
						String a="UMLS:"+cui+":"+tui+":DISO";
						if(!tui.equals("T047")||entry.get(a)==null){
							Document doc = Jsoup.connect("https://www.ncbi.nlm.nih.gov/gtr/conditions/"+cui+"/")
													.ignoreHttpErrors(true)
													.get();
							System.out.println("New download");
							Element frame= doc.getElementsByClass("page_header").first();
							Element mainn = frame.getElementsByTag("h1").first();
							Elements names=frame.getElementsByTag("span");
							String values=mainn.text();
							Iterator<Element> def=names.listIterator();
							while(def.hasNext()){
								values+="|"+((Element)def.next()).text().trim();	
							}
							entry.put(a,values);
							System.out.println("added in the map");
							if(tui.equals("T019")){
								t19++;
							}
							if(tui.equals("T020")){
								t20++;
							}
							if(tui.equals("T046")){
								t46++;
							}
							if(tui.equals("T047")){
								t47++;
							}
							if(tui.equals("T048")){
								t48++;
							}
							if(tui.equals("T049")){
								t49++;
							}
							if(tui.equals("T190")){
								t190++;
							}
							if(tui.equals("T191")){
								t191++;
							}
						}
						System.out.println(t19+t20+t46+t47+t48+t49+t190+t191);
					}
					catch(NullPointerException e){
						System.out.println("UMLS concept skipped "+cui+tui);
					}
					catch(NoSuchElementException e){
						System.out.println("Line skipped");
					}
					catch(SocketTimeoutException e){

					}
				}
		    }
			PrintWriter writer=new PrintWriter("Disorders_UMLS.tsv", "UTF-8");
			System.out.println("writing dictionary");
			while(entry.size()>0){
				String key=(String)entry.lastKey();
				String value=(String)entry.get(key);
				entry.remove(key);
				writer.println(key+"\t"+value);
				i++;
			}
		    writer.close();
		    System.out.println("Printed "+i+" pair entries");
		    System.out.println("Distributed as");
		    System.out.println(t19+" T019");
		    System.out.println(t20+" T020");
		    System.out.println(t46+" T046");
		    System.out.println(t47+" T047");
		    System.out.println(t48+" T048");
		    System.out.println(t49+" T049");
		    System.out.println(t190+" T190");
		    System.out.println(t191+" T191");
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
}