import java.io.*;
import java.util.*;

public class ModelValidator{
	public static void main(String[] args){
		File folder = new File("model_output");
		String[] fileList = folder.list();

		int ml=0; //missing label for annotation
		int na=0; //number of annotations validation
		int np=0; //number of predicted annotation
		int nrp=0; //number of partial annotations
		float nre=0; //number of precise annotations

		try{
			//read input id vocabulary 
			FileReader file=new FileReader(args[1]); //vocabulary
			BufferedReader buffere= new BufferedReader(file);
			Scanner entries=new Scanner(buffere);

			TreeMap<String,ArrayList<String>> labels=new TreeMap<String,ArrayList<String>>();

			//filling vocabulary from id to labels
			while(entries.hasNextLine()){
				String line=entries.nextLine();
				Scanner s=new Scanner(line);
				s.useDelimiter("\t");
				String ed=s.next();
				Scanner e=new Scanner(ed);
				e.useDelimiter(":");
				e.next(); //discard code
				String id=e.next();
				System.out.print("\n"+id+" ");
				labels.put(id,new ArrayList<String>());
				String[] label=s.next().split("\\|");
				for(int i=0;i<label.length;i++){
					System.out.print(label[i]+" ");
					labels.get(id).add(label[i]);
				}
			}

			TreeMap<String,TreeSet<String>> invertedindexlabels=new TreeMap<String,TreeSet<String>>();

			for(String id:labels.keySet()){
				for(String label:labels.get(id)){
					if(invertedindexlabels.get(label)==null) invertedindexlabels.put(label.toLowerCase(),new TreeSet<String>());
					invertedindexlabels.get(label.toLowerCase()).add(id);
				}
			}

			//annotations of validation sentences
			FileReader filevalues=new FileReader(args[0]); //annotations
			BufferedReader bf= new BufferedReader(filevalues);
			Scanner annotations=new Scanner(bf);

			TreeMap<String,ArrayList<String>> sentenceannotationsmapping=new TreeMap<String,ArrayList<String>>(); //sentences and ids

			//filling with sentences and true annotations 
			while(annotations.hasNextLine()){
				String line=annotations.nextLine();
				Scanner adder=new Scanner(line);
				adder.useDelimiter("\t");
				String scode=adder.next();
				adder.useDelimiter("\\p{javaWhitespace}+");
				sentenceannotationsmapping.put(scode,new ArrayList<String>());
				while(adder.hasNext()){
					sentenceannotationsmapping.get(scode).add(adder.next());
				}
				System.out.println(scode+" "+sentenceannotationsmapping.get(scode));
			}
			
			for(String filename: fileList){
				if(filename.endsWith(".ann")){
					FileReader input_file=new FileReader("model_output/"+filename);
					BufferedReader buffer= new BufferedReader(input_file);
					Scanner collecter=new Scanner(buffer);

					TreeSet<String> annots=new TreeSet<String>();
					TreeSet<String> idsannots=new TreeSet<String>();

					String sid=filename.substring(0,filename.length()-4);
					System.out.print("parsing file "+sid+" ");

					//filling with annotations of model
					while(collecter.hasNextLine()){
						String l=collecter.nextLine();
						Scanner labeler=new Scanner(l);
						labeler.useDelimiter("\t");
						if(labeler.next().startsWith("T")){
							labeler.next();
							int n=idsannots.size();
							String label=labeler.next();
							try{
								idsannots.addAll(invertedindexlabels.get(label.toLowerCase()));
							}
							catch(NullPointerException e){
								System.out.println("Sentence "+sid+" -"+label);
								ml++;
								np++;
							}
							annots.add(label);
							if(idsannots.size()>n) np++;
						}
					}
					System.out.println(annots);

					ArrayList<String> codes=sentenceannotationsmapping.get(sid); //all true ids for the sentence
					na+=codes.size();

					for(String id:codes){
						ArrayList<String> lbs=labels.get(id);
						Boolean m=false;
						for(String label:annots){
							for(String lb:lbs){
								if(!m&&label.toLowerCase().equals(lb.toLowerCase())){
									nre++;
									m=true;
								}
							}
							for(String lb:lbs){
								if(!m&&(label.toLowerCase().contains((CharSequence)lb.toLowerCase())||lb.toLowerCase().contains((CharSequence)label.toLowerCase()))){
									nrp++;
									m=true;
								}							
							}
						}
					}
				}
			}

			float recalle=nre/na;
			float precisione=nre/np;
			float recallp=(nrp+nre)/na;
			float precisionp=(nrp+nre)/np;

			System.out.println("Exact match - precision: "+precisione+", recall: "+recalle);
			System.out.println("Partial match - precision: "+precisionp+", recall: "+recallp);
			System.out.println("missing label: "+ml+"/"+np);
		}
		catch(FileNotFoundException e){
			System.out.println("file not present at the specified path");
		}
	}
}