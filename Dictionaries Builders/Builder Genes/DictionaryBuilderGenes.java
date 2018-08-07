import java.io.*;
import java.util.*;

public class DictionaryBuilderGenes{
	public static void main(String[] args){
		int i=0;
		String name="Homo_sapiens.gene_info";
		try{
			FileReader filetrig=new FileReader(name);
			BufferedReader rbuffered = new BufferedReader(filetrig);
			Scanner s=new Scanner(rbuffered);
			PrintWriter dictionary=new PrintWriter(new FileWriter("geneslist_ncbi.tsv", false));
			s.nextLine();
			while(s.hasNextLine()){
				String[] entry=s.nextLine().split("\t");
				String geneid="NCBI:"+entry[1]+":9096:GENE";
				String value=entry[2];
				if(entry[2].equals(entry[10])) value="";
				value+="|"+entry[8]+"|"+entry[10]+"|"+entry[11]+"|"+entry[13]+entry[4];
				if(value.substring(0,1).equals("|")) value=value.substring(1);
				dictionary.println(geneid+"\t"+value);
				i++;
			}
		}
		catch(Exception e){
			System.out.println(e+" "+i);
		}
	}
}