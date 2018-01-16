/**
 * 
 */
package fdi.ucm.server.exportparser.html;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.CompleteLogAndUpdates;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteFile;
import fdi.ucm.server.modelComplete.collection.document.CompleteLinkElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementFile;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementURL;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;


/**
 * @author Joaquin Gayoso-Cabada
 *
 */
public class HTMLprocess {

	private static final int _1000 = 1000;
	protected static final String EXPORTTEXT = "Export HTML RESULT";
	protected ArrayList<List<Long>> ListaDeDocumentosT;
	protected CompleteCollection Salvar;
	protected String SOURCE_FOLDER;
	protected StringBuffer CodigoHTML;
	protected CompleteLogAndUpdates CL;
	private static final Pattern regexAmbito = Pattern.compile("^(ht|f)tp(s)*://(.)*$");
	protected HashMap<String,CompleteElementType> NameCSS;
	protected static final String CLAVY="OdAClavy";

	public HTMLprocess(ArrayList<Long> listaDeDocumentos, CompleteCollection salvar, String sOURCE_FOLDER, CompleteLogAndUpdates cL) {
		ListaDeDocumentosT=new ArrayList<List<Long>>();
		
		if (listaDeDocumentos.isEmpty())
			{
			for (CompleteDocuments Docu : salvar.getEstructuras()) {
				listaDeDocumentos.add(Docu.getClavilenoid());
			}
			}
		
		
		if (listaDeDocumentos.size()<_1000)
			ListaDeDocumentosT.add(listaDeDocumentos);
		else
			{
			for (int i = 0; i < listaDeDocumentos.size(); i=i+_1000) {
				int fin=i+_1000;
				if (fin>listaDeDocumentos.size())
					fin=listaDeDocumentos.size();
				ListaDeDocumentosT.add(listaDeDocumentos.subList(i, fin));
					
				}
			}
		
		
		Salvar=salvar;
		SOURCE_FOLDER=sOURCE_FOLDER;
		CL=cL;
		
		NameCSS=new HashMap<String,CompleteElementType>();
	}

	public void preocess() {
		
		int total=0;

		for (List<Long> ListaDeDocumentos : ListaDeDocumentosT) {
			CodigoHTML=new StringBuffer();
			CodigoHTML.append("<html>");
			CodigoHTML.append("<head>");  
			CodigoHTML.append("<title>"+EXPORTTEXT+"</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"); 
			CodigoHTML.append("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"style.css\">");
			CodigoHTML.append("<meta name=\"description\" content=\"Informe generado por el sistema "+CLAVY+"\">");
			Calendar C=new GregorianCalendar();
			DateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
			String ValueHoy = df.format(C.getTime());	
			CodigoHTML.append("<meta name=\"fecha\" content=\""+ValueHoy+"\">");
			CodigoHTML.append("<meta name=\"author\" content=\"Grupo de investigación ILSA-UCM\">");
//			CodigoHTML.append("<style>");
//			CodigoHTML.append("li.doc {color: blue;}");	
//			CodigoHTML.append("</style>");
			CodigoHTML.append("</head>");  
			CodigoHTML.append("<body>");
			CodigoHTML.append("<ul class\"_List LBody\">");
			
			
			ArrayList<CompleteGrammar> GramaticasAProcesar=ProcesaGramaticas(Salvar.getMetamodelGrammar());
			for (CompleteGrammar completeGrammar : GramaticasAProcesar) {
				ArrayList<CompleteDocuments> Lista=calculadocumentos(completeGrammar,ListaDeDocumentos);
				if (!Lista.isEmpty())
					{
					Lista=ordenaLista(Lista);
					proceraDocumentos(Lista,completeGrammar);
					}
			}
			
			CodigoHTML.append("</ul>");
			CodigoHTML.append("</body>");
			CodigoHTML.append("</html>");
			
			creaLaWeb((ListaDeDocumentosT.size()==1),total,total+500);
			total=total+500;
		}
		
		
		creaLACSS();
		
		
	}

	private ArrayList<CompleteDocuments> ordenaLista(
			ArrayList<CompleteDocuments> lista) {
		quicksort(lista, 0, lista.size()-1);
		return lista;
	}
	
	protected void quicksort(ArrayList<CompleteDocuments> A, int izq, int der) {

		  CompleteDocuments pivote=A.get(izq); // tomamos primer elemento como pivote
		  int i=izq; // i realiza la búsqueda de izquierda a derecha
		  int j=der; // j realiza la búsqueda de derecha a izquierda
		  CompleteDocuments aux;
		 
		  while(i<j){            // mientras no se crucen las búsquedas
		     while(A.get(i).getClavilenoid()<=pivote.getClavilenoid() && i<j) i++; // busca elemento mayor que pivote
		     while(A.get(j).getClavilenoid()>pivote.getClavilenoid()) j--;         // busca elemento menor que pivote
		     if (i<j) {                      // si no se han cruzado                      
		         aux= A.get(i);                  // los intercambia
		         A.set(i, A.get(j));
		         A.set(j,aux);
		     }
		   }
		  A.set(izq,A.get(j)); // se coloca el pivote en su lugar de forma que tendremos
		  A.set(j,pivote); // los menores a su izquierda y los mayores a su derecha
		   if(izq<j-1)
		      quicksort(A,izq,j-1); // ordenamos subarray izquierdo
		   if(j+1 <der)
		      quicksort(A,j+1,der); // ordenamos subarray derecho
		}

	private void creaLACSS() {
		 FileWriter filewriter = null;
		 PrintWriter printw = null;
		    
		try {
			 filewriter = new FileWriter(SOURCE_FOLDER+"\\style.css");//declarar el archivo
		     printw = new PrintWriter(filewriter);//declarar un impresor
		          
		     printw.println("li._Document {color: blue;}");
		     printw.println("span._Type {font-weight: bold;}");
		     printw.println("ul._List {}");
		     printw.println("span._Value {}");
		     
		     printw.close();//cerramos el archivo
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeErrorException(new Error(e), "Error de archivo");
		} 
		
	}

	private void creaLaWeb(boolean unico, int inicio, int fin) {
//		 FileWriter filewriter = null;
//		 PrintWriter printw = null;
		    
		String Name;
		if (unico)
			Name="index";
		else
			Name=inicio+"_"+fin;
		
		try {
			
			File fileDir = new File(SOURCE_FOLDER+"\\"+Name+".html");
			 
			Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileDir), "UTF8"));
			
			out.append(CodigoHTML.toString());
	 
			out.flush();
			out.close();
			
//			 filewriter = new FileWriter(SOURCE_FOLDER+"\\index.html");//declarar el archivo
//		     printw = new PrintWriter(filewriter);//declarar un impresor
//		          
//		     printw.println(CodigoHTML.toString());
//		     
//		     printw.close();//cerramos el archivo
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeErrorException(new Error(e), "Error de archivo");
		} 
		 
		            

		
	}

	protected void proceraDocumentos(ArrayList<CompleteDocuments> lista,
			CompleteGrammar completeGrammar) {
		for (CompleteDocuments completeDocuments : lista) {
			CodigoHTML.append("<li class=\"_Document\"><span class=\"_Type Ident N_0\"> Document: </span><span class=\"Ident _Value N_0V\">"+completeDocuments.getClavilenoid()+"</span></li>");
			CodigoHTML.append("<ul class=\"_List General\">");
			File IconF=new File(SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid());
			IconF.mkdirs();
			
			
			String Path=StaticFunctionsHTML.calculaIconoString(completeDocuments.getIcon());
			
			String[] spliteStri=Path.split("/");
			String NameS = spliteStri[spliteStri.length-1];
			String Icon=SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid()+File.separator+NameS;
			
			try {
				URL url2 = new URL(Path);
				 URI uri2 = new URI(url2.getProtocol(), url2.getUserInfo(), url2.getHost(), url2.getPort(), url2.getPath(), url2.getQuery(), url2.getRef());
				 url2 = uri2.toURL();
				
				saveImage(url2, Icon);
			} catch (Exception e) {
				CL.getLogLines().add("Error in Icon copy, file with url ->>"+completeDocuments.getIcon()+" not found or restringed");
			}
			
			int width= 50;
			int height=50;
			int widthmini= 50;
			int heightmini=50;
			
			try {
				BufferedImage bimg = ImageIO.read(new File(SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid()+File.separator+NameS));
				width= bimg.getWidth();
				height= bimg.getHeight();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			 widthmini= 50;
			 heightmini= (50*height)/width;
			
//			if (width=0)
			
			
			CodigoHTML.append("<li> <span class=\"_Type Icon N_1\">Icon:</span> <img class=\"Icon _Value N_1V\" src=\""+completeDocuments.getClavilenoid()+File.separator+NameS+"\" onmouseover=\"this.width="+width+";this.height="+height+";\" onmouseout=\"this.width="+widthmini+";this.height="+heightmini+";\" width=\""+widthmini+"\" height=\""+heightmini+"\" alt=\""+Path+"\" /></li>");
			CodigoHTML.append("<li> <span class=\"_Type Description N_2\">Description:</span> <span class=\"Description _Value N_0V\">"+completeDocuments.getDescriptionText()+"</span></li>");
			for (CompleteElementType completeST : completeGrammar.getSons()) {
				String Salida = processST(completeST,completeDocuments);
				if (!Salida.isEmpty())
					CodigoHTML.append(Salida);
			}
			
			CodigoHTML.append("</ul>");
			CodigoHTML.append("<br>");
		}
		
		
	}
	
	/**
	 * Salva una imagen dado un destino
	 * @param imageUrl
	 * @param destinationFile
	 * @throws IOException
	 */
	protected void saveImage(URL imageUrl, String destinationFile) throws IOException {

		URL url = imageUrl;
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(destinationFile);

		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}

		is.close();
		os.close();
	}

	private String processST(CompleteElementType completeST,
			CompleteDocuments completeDocuments) {
		StringBuffer StringSalida=new StringBuffer();
		boolean Vacio=true;
			CompleteElement E=findElem(completeST,completeDocuments.getDescription());
			
			
			String tipo = ReduceString(((CompleteElementType)completeST).getName());
			
			if (NameCSS.get(tipo)!=null&&NameCSS.get(tipo)!=completeST)
				tipo=CreateNameCSS(tipo,completeST);
			
			NameCSS.put(tipo,completeST);
			 
			String IDT=((CompleteElementType)completeST).getClavilenoid()+"";
			
			tipo=tipo+" N"+IDT;
			
			
			if (E!=null)
				{
				Vacio=false;
				if (E instanceof CompleteTextElement)
					StringSalida.append("<li> <span class=\"_Type "+tipo+"\">"+((CompleteElementType)completeST).getName()+":</span> <span class=\""+tipo+"V"+" _Value\">"+((CompleteTextElement)E).getValue()+"</span> </li>");
				else if (E instanceof CompleteLinkElement)
					{
					CompleteDocuments Linked=((CompleteLinkElement) E).getValue();
					
					File IconF=new File(SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid());
					IconF.mkdirs();
					
					if (Linked!=null)
					{
						
					String Path=StaticFunctionsHTML.calculaIconoString(Linked.getIcon());
					
					String[] spliteStri=Path.split("/");
					String NameS = spliteStri[spliteStri.length-1];
					String Icon=SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid()+File.separator+NameS;
					
					try {
						URL url2 = new URL(Path);
						 URI uri2 = new URI(url2.getProtocol(), url2.getUserInfo(), url2.getHost(), url2.getPort(), url2.getPath(), url2.getQuery(), url2.getRef());
						 url2 = uri2.toURL();
						
						saveImage(url2, Icon);
					} catch (Exception e) {
						CL.getLogLines().add("Error in Icon copy, file with url ->>"+Linked.getIcon()+" not found or restringed");
					}
					
					
					
					int width= 50;
					int height=50;
					int widthmini= 50;
					int heightmini=50;
					
					try {
						BufferedImage bimg = ImageIO.read(new File(SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid()+File.separator+NameS));
						width= bimg.getWidth();
						height= bimg.getHeight();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
					 widthmini= 50;
					 heightmini= (50*height)/width;
					
					
					StringSalida.append("<li> <<span class=\"_Type "+tipo+"\">"+((CompleteElementType)completeST).getName()+
							": </span> Document Linked -> <img class=\"_ImagenOV "+tipo+"V \" src=\""+
							completeDocuments.getClavilenoid()+File.separator+NameS+
							"\" onmouseover=\"this.width="+width+";this.height="+height+";\" onmouseout=\"this.width="+widthmini+";this.height="+heightmini+
							";\" width=\""+widthmini+"\" height=\""+heightmini+"\" alt=\""+Path+"\" /> "+
							"<span class=\""+tipo+"V _ClavyID _Value\">" +Linked.getClavilenoid()+"</span>"+
							"<span class=\""+tipo+"V _DescriptionRel _Value\">" +Linked.getDescriptionText()+"</span></li>");
					}
					}
				else if (E instanceof CompleteResourceElementURL)
					{
					String Link = ((CompleteResourceElementURL)E).getValue();
							
							if (!testLink(Link))
								Link="http://"+Link;
					StringSalida.append("<li> <span class=\"_Type "+tipo+"\">"+((CompleteElementType)completeST).getName()+": </span>"
									+"<a class=\"_LinkedRef "+tipo+"V "+tipo+"A \" href=\""+Link+"\" target=\"_blank\">"
									+Link+"</a></li>");
					
					}
				else if (E instanceof CompleteResourceElementFile)
					{
					CompleteFile Linked=((CompleteResourceElementFile) E).getValue();
					
					
					File IconF=new File(SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid());
					IconF.mkdirs();
					
					String Path=StaticFunctionsHTML.calculaIconoString(Linked.getPath());
					
					
					String[] spliteStri=Path.split("/");
					String NameS = spliteStri[spliteStri.length-1];
					String Icon=SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid()+File.separator+NameS;
					
					try {
						URL url2 = new URL(Path);
						 URI uri2 = new URI(url2.getProtocol(), url2.getUserInfo(), url2.getHost(), url2.getPort(), url2.getPath(), url2.getQuery(), url2.getRef());
						 url2 = uri2.toURL();
						
						saveImage(url2, Icon);
					} catch (Exception e) {
						CL.getLogLines().add("Error in Icon copy, file with url ->> "+Linked.getPath()+" not found or restringed");
					}
					
					int width= 50;
					int height=50;
					int widthmini= 50;
					int heightmini=50;
					
					try {
						BufferedImage bimg = ImageIO.read(new File(SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid()+File.separator+NameS));
						width= bimg.getWidth();
						height= bimg.getHeight();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
					 widthmini= 50;
					 heightmini= (50*height)/width;
					
					StringSalida.append("<li> <span class=\"_Type "+tipo+"\">"+((CompleteElementType)completeST).getName()+":</span> " +
							"File Linked -> <img class=\"_ImagenFile "+tipo+"V \" class=\"ImagenOV\" src=\""+completeDocuments.getClavilenoid()+File.separator+NameS+"\"" +
									" onmouseover=\"this.width="+width+";this.height="+height+";\" onmouseout=\"this.width="+widthmini+";this.height="+heightmini+";\" width=\""+widthmini+"\" height=\""+heightmini+"\" alt=\""+Path+"\" />" +
									"<a class=\"_LinkedRef "+tipo+"V "+tipo+"A  \" href=\""+completeDocuments.getClavilenoid()+File.separator+NameS+"\" target=\"_blank\">"+
									NameS+"</a></li>");				

					}
				else 
				{
					if (completeST.isSelectable())
						StringSalida.append("<li> <span class=\"_Value "+tipo+"\">"+((CompleteElementType)completeST).getName()+"</span></li>");

				}
//				else 
//					Vacio=true;
				
				}
			else
				Vacio=true;
			
			StringBuffer Hijos=new StringBuffer();
			for (CompleteElementType hijo : completeST.getSons()) {
				
				String result2 = processST(hijo, completeDocuments);
				
				if (!result2.isEmpty())
					Hijos.append(result2.toString());	
			}
			
			
			String HijosSalida = Hijos.toString();
			
			if (!HijosSalida.isEmpty()&&Vacio)
			{
			StringSalida.append("<li> <span class=\"_Type "+tipo+"\">"+((CompleteElementType)completeST).getName()+":</span> </li>");
			
			}
		
		if (!HijosSalida.isEmpty())
			{
			StringSalida.append("<ul class=\"_List "+tipo+"\">");
			StringSalida.append(HijosSalida);
			StringSalida.append("</ul>");
			}
			

	
		
		
		return StringSalida.toString();
		
	}



//	protected HashSet<Integer> calculaAmbitos(ArrayList<Integer> ambitos,
//			CompleteElementType completeST, CompleteDocuments completeDocuments) {
//		HashSet<Long> hijos=new HashSet<Long>();
//		calculaHijos(completeST,hijos);
//		HashSet<Integer> Salida=new HashSet<Integer>();
//		int ultimo=ambitos.size();
//		for (CompleteElement element : completeDocuments.getDescription()) {
//			if (hijos.contains(element.getHastype().getClavilenoid())&&element.getAmbitos().size()>ultimo)
//				if (!Salida.contains(element.getAmbitos().get(ultimo)))
//					Salida.add(element.getAmbitos().get(ultimo));
//				
//				
//		}
//		return Salida;
//	}
//
//	private void calculaHijos(CompleteElementType completeST, HashSet<Long> hijos) {
//		if (!hijos.contains(completeST.getClavilenoid()))
//			hijos.add(completeST.getClavilenoid());
//		for (CompleteElementType hijo : completeST.getSons()) {
//			calculaHijos(hijo,hijos);
//		}
//	}
//
//	protected CompleteElement findElem(CompleteElementType completeST, List<CompleteElement> description,
//			ArrayList<Integer> ambitos) {
//		for (CompleteElement elementos : description) {
//			if (elementos.getHastype().getClavilenoid().equals(completeST.getClavilenoid())&&validos(elementos.getAmbitos(),ambitos))
//				return elementos;
//		}
//		return null;
//	}
//
//	private boolean validos(ArrayList<Integer> documento,
//			ArrayList<Integer> actual) {
//		if (actual.size()>documento.size())
//			return false;
//		
//		for (int i = 0; i < actual.size(); i++) {
//			if (!actual.get(i).equals(documento.get(i)))
//				return false;
//		}
//		
//		return true;
//	}

	
	protected CompleteElement findElem(CompleteElementType completeST, List<CompleteElement> description) {
for (CompleteElement elementos : description) {
	if (elementos.getHastype().getClavilenoid().equals(completeST.getClavilenoid()))
		return elementos;
}
return null;
}
	
	protected ArrayList<CompleteDocuments> calculadocumentos(
			CompleteGrammar completeGrammar,List<Long> ListaDeDocumentos) {
		ArrayList<CompleteDocuments> Salida=new ArrayList<CompleteDocuments>();
		for (CompleteDocuments iterable_element : Salvar.getEstructuras()) {
			if (ListaDeDocumentos.isEmpty()||(ListaDeDocumentos.contains(iterable_element.getClavilenoid())&&StaticFunctionsHTML.isInGrammar(iterable_element,completeGrammar)))
				Salida.add(iterable_element);
		}
		return Salida;
	}

	protected ArrayList<CompleteGrammar> ProcesaGramaticas(
			List<CompleteGrammar> metamodelGrammar) {
		ArrayList<CompleteGrammar> Salida=new ArrayList<CompleteGrammar>();
		for (CompleteGrammar completeGrammar : metamodelGrammar) {
			Salida.add(completeGrammar);
		}
		return Salida;
	}
	
	public static boolean testLink(String baseURLOda2) {
		if (baseURLOda2==null||baseURLOda2.isEmpty())
			return true;
		 Matcher matcher = regexAmbito.matcher(baseURLOda2);
		return matcher.matches();
	}
	
	
	protected String ReduceString(String description) {
		StringBuffer SB=new StringBuffer();
		for (int i = 0; i < description.length() && SB.length()<25; i++) {
			if ((description.charAt(i)>='A'&&description.charAt(i)<='z')||(description.charAt(i)>='A'&&description.charAt(i)<='Z'))
				SB.append(description.charAt(i));
		}
		return SB.toString();
	}
	
	
	protected String CreateNameCSS(String tipo, CompleteElementType completeST) {
		int i=0;
		String Base=tipo;
		while (NameCSS.get(Base+i)!=null&&NameCSS.get(Base+i)!=completeST)
			i++;
		return Base+i;
	}

}
