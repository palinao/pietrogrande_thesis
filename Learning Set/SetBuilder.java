import java.io.*;
import java.util.*;

public class SetBuilder{
	public static void main(String[] args){
		String name="../Additional_files/nanopublications_v4.0.0.0.trig";
		int in=0;//index of the sentence
		int nu=0; //number of unique sentences
		int midg=0; //missing id for genes
		int mide=0; //missing id for dises
		int mlag=0; //missing label for genes
		int mlae=0; //missing label for dises
		int stg=0; //number sentences training genes
		int ste=0; //number sentences training dises
		int svg=0; //number sentences validation genes
		int sve=0; //number sentences validation dises
		int gt=0; //number of genes annotation training
		int gv=0; //number of genes annotation validation
		int et=0; //number of dises annotation training
		int ev=0; //number of dises annotation validation

		try{ 
			//read input publications file
			FileReader filetrig=new FileReader(name);
			BufferedReader rbuffered = new BufferedReader(filetrig);
			Scanner parser=new Scanner(rbuffered);

			//writers for sentences training set
			PrintWriter writersg=new PrintWriter(new FileWriter("Genes data/Training set/sentencesfortraininggenes",true));
			PrintWriter writerse=new PrintWriter(new FileWriter("Disorders data/Training set/sentencesfortrainingdises",true));

			//writers for annotations training set
			PrintWriter writerag=new PrintWriter(new FileWriter("Genes data/Training set/annotationsfortraininggenes",true));
			PrintWriter writerae=new PrintWriter(new FileWriter("Disorders data/Training set/annotationsfortrainingdises",true));

			//writers for annotations validation set
			PrintWriter writermg=new PrintWriter(new FileWriter("Genes data/Metafiles/validationcodesgenes.tsv",true));
			PrintWriter writerme=new PrintWriter(new FileWriter("Disorders data/Metafiles/validationcodesdises.tsv",true));

			TreeMap<String,Integer> samples=new TreeMap<String,Integer>(); //all the manually analyzed sentences with an id
			TreeMap<Integer,ArrayList<String>> annotationsg=new TreeMap<Integer,ArrayList<String>>(); //lists of genes labels for a sentence
			TreeMap<Integer,ArrayList<String>> annotationse=new TreeMap<Integer,ArrayList<String>>(); //lists of dises labels for a sentence
			TreeMap<Integer,ArrayList<String>> sentencegenesmapping=new TreeMap<Integer,ArrayList<String>>(); //list of genes ids in a sentence
			TreeMap<Integer,ArrayList<String>> sentencedisesmapping=new TreeMap<Integer,ArrayList<String>>(); //list of dises ids in a sentence

			//read input genes vocabulary 
			FileReader filegene=new FileReader("genes_homosapiens.tsv");
			BufferedReader buffere= new BufferedReader(filegene);
			Scanner genes=new Scanner(buffere);

			//read input dises vocabulary
			FileReader filediso= new FileReader("Disorders_UMLS.tsv");
			BufferedReader bufferi=new BufferedReader(filediso);
			Scanner dises=new Scanner(bufferi);

			TreeMap<String,String[]> geneslist=new TreeMap<String,String[]>(); //Genes id and labels
			TreeMap<String,String[]> diseslist=new TreeMap<String,String[]>(); //Dises id and labels

			//filling genes vocabulary
			while(genes.hasNextLine()){
				String line=genes.nextLine();
				Scanner s=new Scanner(line);
				s.useDelimiter("\t");
				String gedi=s.next();
				Scanner e=new Scanner(gedi);
				e.useDelimiter(":");
				e.next();
				String geneid=e.next();
				System.out.print("\n"+geneid+" ");
				String[] label=s.next().split("\\|");
				for(int i=0;i<label.length;i++){
					System.out.print(label[i]+" ");
				}
				// System.out.print("\n"+geneid+" ");
				// for(int i=0;i<label.length;i++){
				// 	System.out.println(label[i]);
				// }
				geneslist.put(geneid,label);
			}

			//filling disorders vocabulary
			while(dises.hasNextLine()){
				String line=dises.nextLine();
				Scanner s=new Scanner(line);
				s.useDelimiter("\t");
				String disord=s.next().substring(5,13);
				String[] label=s.next().split("\\|");
				// System.out.print("\n"+disord+" ");
				// for(int i=0;i<label.length;i++){
				// 	System.out.print(label[i]);
				// }
				diseslist.put(disord,label);
			}

			//parse for a single nanopublication
			parser.useDelimiter("@prefix this");
			parser.next(); //discard empty raw
			String generationmode=null;
			
			while(parser.hasNext()){
				String nanop=parser.next(); //a single nanopublication
				Scanner s=new Scanner(nanop);
				s.useDelimiter("\\["); //sentences are in squared brackets
				s.next(); //shift to sentence
				s.useDelimiter(".]");
				String sentence=s.next().substring(1).trim(); //sentence to be annotated
				Integer old=null; //the old id of the sentence
				try{
					s.useDelimiter("manually");
					s.next();//jump to comment
					s.useDelimiter(";");
					generationmode=s.next(); //if may throw an exception
					s.close();
					in++; // the nanopublication was manually curated
					old=samples.get(sentence);
					if(old==null) samples.put(sentence,in); //if the sentence was not present it is added
					Integer index=in; 
					if(old!=null) index=old;
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
					String geneid=af.next().substring(12);
					af.useDelimiter("lld:");
					af.next(); //discard initial part
					af.useDelimiter(" ");
					String disorderid=af.next().substring(4);
					String[] labelg=geneslist.get(geneid); //may be null if the gene id is not present in the vocabulary
					String[] labels=diseslist.get(disorderid); //may be null if the disorder id is not present in the vocabulary
					String[] label=new String[2]; //labels annotated in the sentence 
					int i=0;
					label[0]=null; //label for genes
					try{
						while(i<labelg.length){
							//pattern matching between the possible labels and the sentence
							int start=sentence.toLowerCase().indexOf(labelg[i].toLowerCase().trim());
							if(start>=0&&(label[0]==null||labelg[i].length()>label[0].length())){
								label[0]=labelg[i];
							}
							i++;
							//if the sentence has no annotation it is printed
							if(i==labelg.length){ 
								System.out.println("\nS: "+sentence);
								for(int j=0;j<labelg.length;j++){
									System.out.print("-"+labelg[j]+" ");
								}
							}
						}
					}
					catch(NullPointerException e){
						if(old==null){
							midg++; //the id is missing
							mlag--; //balance the missing label added
						}
					}

					i=0;
					label[1]=null; //label for disorders
					try{
						while(i<labels.length){
							//pattern matching between the possible labels and the sentence
							int start=sentence.toLowerCase().indexOf(labels[i].toLowerCase().trim());
							if(start>=0&&(label[1]==null||labels[i].length()>label[1].length())){
								label[1]=labels[i];
							}
							i++;
							//if the sentence has no annotation it is printed
							if(i==labels.length){
								System.out.println("\nS: "+sentence);
								for(int j=0;j<labels.length;j++){
									System.out.print("-"+labels[j]+" ");
								}
							}
						}
					}
					catch(NullPointerException e){
						if(old==null){
							mide++; //the id is missing
							mlae--; //balance the missing label added
						}
					}

					if(label[0]!=null){
						if((annotationsg.get(index))==null) annotationsg.put(index,new ArrayList<String>()); 
						if(!annotationsg.get(index).contains(label[0])) annotationsg.get(index).add(label[0]); //add label only if it is a new sentence
						if((sentencegenesmapping.get(index))==null) sentencegenesmapping.put(index,new ArrayList<String>());
						if(!sentencegenesmapping.get(index).contains(geneid)) sentencegenesmapping.get(index).add(geneid); //add gene only if it is a new sentence
					}
					else{
						if(old==null) mlag++; //gene label is missing
					}
					if(label[1]!=null){
						if((annotationse.get(index))==null) annotationse.put(index,new ArrayList<String>());
						if(!annotationse.get(index).contains(label[1])) annotationse.get(index).add(label[1]); //add label only if it is a new sentence
						if((sentencedisesmapping.get(index))==null) sentencedisesmapping.put(index,new ArrayList<String>());
						if(!sentencedisesmapping.get(index).contains(disorderid)) sentencedisesmapping.get(index).add(disorderid); //add gene only if it is a new sentence
					} 
					else{
						if(old==null) mlae++; //disorder label is missing 
					}
				}
				catch(NoSuchElementException e){

				}
			}
			nu=samples.size();
			System.out.println("Writing sets");
			Random rand=new Random();
			while(samples.size()>0){
				int n=rand.nextInt(100);
				Map.Entry f=samples.lastEntry(); //pair sentence and id
				int id=(Integer)f.getValue();
				try{
					String phrase=(String)f.getKey();
					if(n>20){
						ArrayList<String> labelsg=annotationsg.get(id); //labels in the sentence
						for(String labelg:labelsg){ //possible null pointer e
							int posig=phrase.toLowerCase().replace(" ","").indexOf(labelg.toLowerCase().replace(" ","")); //initial position in the sentence without empty spaces
							int posfg=posig+labelg.replace(" ","").length()-1; //ending position in the sentence without empty spaces
							writerag.printf("P%08d|%d %d|%s\n",id,posig,posfg,labelg);
							gt++;
						}
						writersg.printf("P%08d %s\n",id,phrase);
						stg++;
					} 
					else{
						ArrayList<String> codes=sentencegenesmapping.get(id);
						String genescodes="";
						for(String value: codes){ //it throws null pointer exception
							genescodes+=value+" ";
							gv++;
						}
						genescodes=genescodes.trim();
						writermg.printf("P%08d\t%s\n",id,genescodes);
						String filenumber=String.format("P%08d",id);
						PrintWriter writervg=new PrintWriter(new FileWriter("Genes data/Validation set/"+filenumber+".txt",true));
						writervg.println(phrase);
						svg++;
						writervg.close();
					}
				}
				catch(NullPointerException e){
					System.out.println("No annotations "+id);
				}
				try{
					String phrase=(String)f.getKey();
					if(n>20){
						ArrayList<String> labelse=annotationse.get(id); //labels in the sentence
						for(String labele:labelse){ //possible null pointer e
							int posie=phrase.toLowerCase().replace(" ","").indexOf(labele.toLowerCase().replace(" ","")); //initial position in the sentence without empty spaces
							int posfe=posie+labele.replace(" ","").length()-1; //initial position in the sentence without empty spaces
							writerae.printf("P%08d|%d %d|%s\n",id,posie,posfe,labele);
							et++;
						}
						writerse.printf("P%08d %s\n",id,phrase);
						ste++;
					}
					else{
						ArrayList<String> codes=sentencedisesmapping.get(id);
						String disescodes="";
						for(String value: codes){ //it throws null pointer exception
							disescodes+=value+" ";
							ev++;
						}
						disescodes=disescodes.trim();
						writerme.printf("P%08d\t%s\n",id,disescodes);
						String filenumber=String.format("P%08d",id);
						PrintWriter writerve=new PrintWriter(new FileWriter("Disorders data/Validation set/"+filenumber+".txt",true));
						writerve.println(phrase);
						sve++;
						writerve.close();
					}
				}
				catch(NullPointerException e){
					System.out.println("No annotations "+id);
				}
				samples.remove(f.getKey());
			}
			writersg.close();
			writerse.close();
			writerae.close();
			writerag.close();
			writerme.close();
			writermg.close();

			PrintWriter writervg=new PrintWriter(new FileWriter("Genes data/Metafiles/statisticsgenes.txt",true));
			PrintWriter writerve=new PrintWriter(new FileWriter("Disorders data/Metafiles/statisticsdises.txt",true));

			writervg.println("Number of available sentences: "+nu);
			mlag=nu-stg-svg-midg;
			writerve.println("Number of available sentences: "+nu);
			mlae=nu-ste-sve-mide;

			writervg.println("Sentences in the training set: "+stg+" ("+gt+" annotated genes)"
								+"\nSentences in the validation set: "+svg+" ("+gv+" annotated genes)"
								+"\nSentences with missing diso id: "+midg
								+"\nSentences with no label match: "+mlag);
			writerve.println("Sentences in the training set: "+ste+" ("+et+" annotated disorders)"
								+"\nSentences in the validation set: "+sve+" ("+ev+" annotated disorders)"
								+"\nSentences with missing diso id: "+mide
								+"\nSentences with no label match: "+mlae);

			writervg.close();
			writerve.close();
		}
		catch(FileNotFoundException e){
			System.out.println(e);	
		} 
		catch(IOException e){
			System.out.println(e);
		}
	}		
}