import java.io.*;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SourceLoader{
	public static void main(String[] args){
		String name="../Additional_files/nanopublications_v4.0.0.0.trig";
		
		//the number of file to be added to the dataset
		final int MAX_DIMENSION=Integer.parseInt(args[0]);
		
		TreeSet<String> geneassociations=new TreeSet<String>();
		TreeSet<String> completeassociations=new TreeSet<String>();
		TreeSet<String> diset=new TreeSet<String>();
		TreeSet<String> giset=new TreeSet<String>();
		int m=0; //manual
		int i=0; //added
		int a=0; //automa
		int p=0; //papers
		int n=0; //number
		int f=0; //folder

		try{ 
			//read input publications file
			FileReader filetrig=new FileReader(name);
			BufferedReader rbuffered = new BufferedReader(filetrig);
			Scanner parser=new Scanner(rbuffered);
			String id=null;

			//parse for a single nanopublication
			parser.useDelimiter("@prefix this");
			parser.next(); //discard empty raw
			int folders=0;
			String generationmode=null;
			boolean skip=false;
			File directory=null;

			try{
				while(parser.hasNext()){
					String nanop=parser.next(); //a single nanopublication
					Thread.sleep(100); //for multithreaded synchronization
					Scanner s=new Scanner(nanop);
					n++;
					s.useDelimiter("SIO_000772 miriam-pubmed:");
					s.next();//jump to article code
					s.useDelimiter(";");
					id=s.next().substring(25).trim(); //article id
					s.useDelimiter("manually");
					s.next();//jump to comment
					s.useDelimiter(";");
					generationmode=s.next();
					System.out.println(id);
					s.close();
					s=new Scanner(nanop);
					s.useDelimiter("sio:SIO_");
					s.next(); //discard initial part
					s.useDelimiter(" ");
					completeassociations.add(s.next());
					s.useDelimiter("sio:SIO_");
					s.next();
					s.useDelimiter(" ");
					completeassociations.add(s.next());

					//elaborate just manually curated nanopubs
					if(generationmode.equals("manually curated.\"@en ")){ 
						File file = new File("nanopubs/dataset/"+id);
						m++;

						Scanner af=new Scanner(nanop);
						af.useDelimiter("_assertion");
						af.next();
						af.useDelimiter("miriam-gene");
						af.next(); //discard initial part
						af.useDelimiter("}");
						String assertion="{"+af.next()+"}\n";
						af=new Scanner(assertion);
						af.useDelimiter("miriam-gene:");
						af.next(); //discard initial part
						af.useDelimiter(" ");
						giset.add(af.next());
						af.useDelimiter("lld:");
						af.next(); //discard initial part
						af.useDelimiter(" ");
						diset.add(af.next());
						af.useDelimiter("sio:SIO_");
						af.next(); //discard initial part
						af.useDelimiter(" ");
						geneassociations.add(af.next());
						af.useDelimiter("sio:SIO_");
						af.next();
						af.useDelimiter(" ");
						geneassociations.add(af.next());

						if(file.exists()){
							//append nanopub if file already exists
							PrintWriter writerI = new PrintWriter(new FileWriter("nanopubs/dataset/"+id+"/"+id+".trig", true));
							writerI.println(assertion);
							writerI.close();
							System.out.println("Updated file "+id);
						}
						if(f<=MAX_DIMENSION&&!file.exists()){
							p++;
							try{
								directory= new File("nanopubs/dataset/"+id);
								directory.mkdir();
								f++; //a new folder has been created

								//dowload the html source for the article
								Document doc = Jsoup.connect("http://identifiers.org/pubmed/"+id)
												.timeout(0).ignoreHttpErrors(true)
												.get();

								Element content = doc.getElementsByClass("rprt abstract").first();
								Element as = content.getElementsByClass("auths").first(); 
								Element title = content.getElementsByTag("h1").first();
								Element text = content.getElementsByTag("p").first();
								PrintWriter writer = new PrintWriter("nanopubs/dataset/"+id+"/"+id+".txt", "UTF-8");
								writer.println(title.text());
								writer.println(text.text());
								writer.close();
								PrintWriter writerI = new PrintWriter(new FileWriter("nanopubs/dataset/"+id+"/"+id+".trig", true));
								writerI.println(assertion);
								writerI.close();

								System.out.println("New folder "+id);
							}
							catch(NullPointerException e){
								System.out.println(e+" File discarded "+id);
							}
						}
					}
					else{
						a++;
						System.out.println("File discarded for generation "+id);
					}
				}

				PrintWriter stats = new PrintWriter("nanopub/statistics/statistics.info", "UTF-8");
				stats.println("Number of assertions in the source file: "+n);
				stats.println("Number of assertions in the dataset: "+i);
				stats.println("Number of folders: "+f);
				stats.println("Number of manually curated assertions in the source file: "+m);
				stats.println("Number of automatically generated assertions in the source file: "+a);
				stats.println("Number of papers in the source file: "+p);

				while(!geneassociations.isEmpty()){
					String saa=geneassociations.first();
					stats.print(saa);
					geneassociations.remove(saa);
				}

				while(!completeassociations.isEmpty()){
					String saa=completeassociations.first();
					stats.print(saa);
					completeassociations.remove(saa);
				}

				stats.close();

			}
			catch(IOException e){
				System.out.println(e);
			}
		}
		catch(FileNotFoundException e){
			System.out.println(e);	
		} 
		catch(InterruptedException e){
			System.out.println(e);	
		}
		catch(Exception e){
			System.out.println(e);	
		}
		finally{
			PrintWriter stats=null;
			try{
				stats = new PrintWriter("nanopubs/statistics/statistics.info", "UTF-8");
			}
			catch(FileNotFoundException e){
				System.out.println(e);	
			} 
			catch(Exception e){
				System.out.println(e);	
			}
			stats.println("Number of assertions in the source file: "+n);
			stats.println("Number of assertions in the dataset: "+i);
			stats.println("Number of folders: "+f);
			stats.println("Number of manually curated assertions in the source file: "+m);
			stats.println("Number of automatically generated assertions in the source file: "+a);
			stats.println("Number of papers in the source file: "+p);
			
			while(!geneassociations.isEmpty()){
				String saa=geneassociations.first();
				stats.print(saa);
				geneassociations.remove(saa);
			}

			while(!completeassociations.isEmpty()){
				String saa=completeassociations.first();
				stats.print(saa);
				completeassociations.remove(saa);
			}

			while(!diset.isEmpty()){
				String saa=diset.first();
				stats.println(saa);
				diset.remove(saa);
			}

			while(!giset.isEmpty()){
				String saa=giset.first();
				stats.println(saa);
				giset.remove(saa);
			}

			stats.close();
		}
	}		
}