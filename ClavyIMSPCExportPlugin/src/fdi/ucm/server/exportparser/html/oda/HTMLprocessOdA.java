/**
 * 
 */
package fdi.ucm.server.exportparser.html.oda;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.imageio.ImageIO;

import fdi.ucm.server.exportparser.html.HTMLprocess;
import fdi.ucm.server.exportparser.html.StaticFunctionsHTML;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.CompleteLogAndUpdates;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteLinkElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementFile;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementURL;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteResourceElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

/**
 * @author Joaquin Gayoso-Cabada
 *
 */
public class HTMLprocessOdA extends HTMLprocess {

	protected static final String EXPORTTEXT = "Resultado de Exportacion en HTML";
	private HashSet<Long> AdministradorListaDocumentos;
	private HashSet<Long> AdministradorListaStructura;
	
	private boolean Administrador;


	/**
	 * @param listaDeDocumentos
	 * @param salvar 
	 * @param sOURCE_FOLDER 
	 * @param cL 
	 * @param listaStructura 
	 * @param b 
	 * @param arrayList 
	 */
	public HTMLprocessOdA(ArrayList<Long> listaDeDocumentos, CompleteCollection salvar, String sOURCE_FOLDER, CompleteLogAndUpdates cL, ArrayList<Long> administradorList, boolean administrador, ArrayList<Long> listaStructura) {
		super(listaDeDocumentos,salvar,sOURCE_FOLDER,cL);
		
		AdministradorListaDocumentos=new HashSet<Long>();
		for (Long long1 : administradorList) {
			if (!AdministradorListaDocumentos.contains(long1))
				AdministradorListaDocumentos.add(long1);
		}
		Administrador=administrador;
		
		AdministradorListaStructura=new HashSet<Long>();
		for (Long long1 : listaStructura) {
			if (!AdministradorListaStructura.contains(long1))
				AdministradorListaStructura.add(long1);
		}

		
	}
	
	@Override
	protected void quicksort(ArrayList<CompleteDocuments> A, int izq, int der) {

		  CompleteDocuments pivote=A.get(izq); // tomamos primer elemento como pivote
		  int i=izq; // i realiza la búsqueda de izquierda a derecha
		  int j=der; // j realiza la búsqueda de derecha a izquierda
		  CompleteDocuments aux;
		 
		  while(i<j){            // mientras no se crucen las búsquedas
		     while(getIDOV(A.get(i))<=getIDOV(pivote) && i<j) i++; // busca elemento mayor que pivote
		     while(getIDOV(A.get(j))>getIDOV(pivote)) j--;         // busca elemento menor que pivote
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
	
	private Long getIDOV(CompleteDocuments completeDocuments) {
		Long IDOV=completeDocuments.getClavilenoid();
		for (CompleteElement elemetpos : completeDocuments.getDescription()) {
			if (elemetpos instanceof CompleteTextElement&&elemetpos.getHastype() instanceof CompleteTextElementType&&StaticFuctionsHTMLOdA.isIDOV((CompleteTextElementType)elemetpos.getHastype()))
				{
				String IDOVS=((CompleteTextElement) elemetpos).getValue();
				try {
					
					IDOV=Long.parseLong(IDOVS);
					return IDOV;
				} catch (Exception e) {
					System.err.println("error de un entero que es "+IDOVS);
					e.printStackTrace();
				
				}
				}
		}
		return IDOV;
		
	}

	@Override
	protected ArrayList<CompleteGrammar> ProcesaGramaticas(
			List<CompleteGrammar> metamodelGrammar) {
		ArrayList<CompleteGrammar> Salida=new ArrayList<CompleteGrammar>();
		for (CompleteGrammar completeGrammar : metamodelGrammar) {
			if (StaticFuctionsHTMLOdA.isVirtualObject(completeGrammar))
				Salida.add(completeGrammar);
		}
		return Salida;
	}
	
	@Override
	protected ArrayList<CompleteDocuments> calculadocumentos(
			CompleteGrammar completeGrammar,List<Long> ListaDeDocumentos) {
		ArrayList<CompleteDocuments> Salida1=new ArrayList<CompleteDocuments>();
		for (CompleteDocuments iterable_element : Salvar.getEstructuras()) {
			if (inListNormal(iterable_element,ListaDeDocumentos)&&StaticFuctionsHTMLOdA.isInGrammar(iterable_element,completeGrammar))
				Salida1.add(iterable_element);
		}
		
		ArrayList<CompleteDocuments> Salida=new ArrayList<CompleteDocuments>();
		for (CompleteDocuments completeDocuments : Salida1) {
			if (StaticFuctionsHTMLOdA.getPublic(completeDocuments,completeGrammar)||Administrador||inList(completeDocuments))
				Salida.add(completeDocuments);
		}
		
		
		return Salida;
	}
	
	private boolean inListNormal(CompleteDocuments completeDocuments,List<Long> ListaDeDocumentos) {
		String IDOV=completeDocuments.getClavilenoid()+"";
		for (CompleteElement elemetpos : completeDocuments.getDescription()) {
			if (elemetpos instanceof CompleteTextElement&&elemetpos.getHastype() instanceof CompleteTextElementType&&StaticFuctionsHTMLOdA.isIDOV((CompleteTextElementType)elemetpos.getHastype()))
				IDOV=((CompleteTextElement) elemetpos).getValue();
		}
		
		try {
			Long IDOVL=Long.parseLong(IDOV);
			if (ListaDeDocumentos.contains(IDOVL))
				return true;
			else 
				return false;
		} catch (Exception e) {
			return false;
		}
		
		
	}
	
	private boolean inList(CompleteDocuments completeDocuments) {
		String IDOV=completeDocuments.getClavilenoid()+"";
		for (CompleteElement elemetpos : completeDocuments.getDescription()) {
			if (elemetpos instanceof CompleteTextElement&&elemetpos.getHastype() instanceof CompleteTextElementType&&StaticFuctionsHTMLOdA.isIDOV((CompleteTextElementType)elemetpos.getHastype()))
				IDOV=((CompleteTextElement) elemetpos).getValue();
		}
		
		try {
			Long IDOVL=Long.parseLong(IDOV);
			if (AdministradorListaDocumentos.contains(IDOVL))
				return true;
			else 
				return false;
		} catch (Exception e) {
			return false;
		}
		
		
	}

	@Override
	protected void proceraDocumentos(ArrayList<CompleteDocuments> lista,
			CompleteGrammar completeGrammar) {
		
		 String Description=StaticFuctionsHTMLOdA.getDescription(completeGrammar);
		 
		  if (Description.isEmpty())
			 Description="Descripción";
		  
		 String DescriptionR = ReduceString(Description);
		 CompleteElementType  ReferenciaDesc=new CompleteElementType(DescriptionR, completeGrammar);
			
			NameCSS.put(DescriptionR, ReferenciaDesc);
		
		for (CompleteDocuments completeDocuments : lista) {
			
			String IDOV=completeDocuments.getClavilenoid()+"";
			for (CompleteElement elemetpos : completeDocuments.getDescription()) {
				if (elemetpos instanceof CompleteTextElement&&elemetpos.getHastype() instanceof CompleteTextElementType&&StaticFuctionsHTMLOdA.isIDOV((CompleteTextElementType)elemetpos.getHastype()))
					IDOV=((CompleteTextElement) elemetpos).getValue();
			}
			CodigoHTML.append("<li class=\"_Document Objetos N_R\"> <span class=\"_Type Identificador N_0\">Objeto Digital: </span><span class=\"Ident _Value N_0V\">"+IDOV+" </span></li>");
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
				CL.getLogLines().add("Error en copia del icono con url ->>"+completeDocuments.getIcon()+" not encontrado o restringido");
			}
			
			int width= 150;
			int height=150;
			int widthmini= 150;
			int heightmini=150;
			int widthmaxi= 150;
			int heightmaxi=150;
			
			try {
				BufferedImage bimg = ImageIO.read(new File(SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid()+File.separator+NameS));
				width= bimg.getWidth();
				height= bimg.getHeight();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			 widthmini= 150;
			 heightmini= (150*height)/width;
			 
			 widthmaxi= 600;
			 heightmaxi= (600*height)/width;
			
//			if (width=0)
			
			 
			 
			CodigoHTML.append("<li> <span class=\"_Type Icono N_1\">Icono:</span> <img class=\"ImagenIcono _Value N_1V\"src=\""+completeDocuments.getClavilenoid()+File.separator+NameS+"\" onmouseover=\"this.width="+widthmaxi+";this.height="+heightmaxi+";\" onmouseout=\"this.width="+widthmini+";this.height="+heightmini+";\" width=\""+widthmini+"\" height=\""+heightmini+"\" alt=\""+Path+"\" /></li>");
			CodigoHTML.append("<li> <span class=\"_Type "+DescriptionR+"\">"+Description+":</span> <span class=\""+DescriptionR+"V \">"+completeDocuments.getDescriptionText()+"</span></li>");
			
			
			ArrayList<CompleteElementType> OdAElements=findOdAElements(completeGrammar.getSons());
			
			
			for (CompleteElementType completeST : OdAElements) {
				String Salida="";
				if (completeST instanceof CompleteElementType&&(StaticFuctionsHTMLOdA.isDatos((CompleteElementType)completeST)||StaticFuctionsHTMLOdA.isMetaDatos((CompleteElementType)completeST)))
						Salida = processSTDatosYMeta(completeST,completeDocuments);
				else if (completeST instanceof CompleteElementType&&StaticFuctionsHTMLOdA.isResources((CompleteElementType)completeST))
						{
						StringBuffer StringSalida=new StringBuffer();
			
							StringBuffer Hijos=new StringBuffer();
							
							String result2="";

									result2 = processSTRecursos(completeST, completeDocuments);
								
								if (!result2.isEmpty())
									Hijos.append(result2.toString());	

							String HijosSalida = Hijos.toString();
							
							if (!HijosSalida.isEmpty())
							{

							StringSalida.append(HijosSalida);

							}

						String ST = StringSalida.toString();
						StringBuffer StringSalidaFinal = new StringBuffer();
						if (!ST.isEmpty())
							{
							
							String tipo = ReduceString(((CompleteElementType)completeST).getName());
							
							if (NameCSS.get(tipo)!=null&&NameCSS.get(tipo)!=completeST)
								tipo=CreateNameCSS(tipo,completeST);
							
							NameCSS.put(tipo,completeST);
							 
							String IDT=((CompleteElementType)completeST).getClavilenoid()+"";
							
							Integer IDT2 = StaticFuctionsHTMLOdA.getIDODAD(((CompleteElementType)completeST));
							if (IDT2!=null)
								IDT=IDT2+"";
							
							tipo=tipo+" N"+IDT;
							
							StringSalidaFinal.append("<li> <span class=\"_Type "+tipo+"\">"+((CompleteElementType)completeST).getName()+": </span></li>");
							StringSalidaFinal.append("<ul class=\"_List "+tipo+"\">");
							StringSalidaFinal.append(ST);
							StringSalidaFinal.append("</ul>");
							}
						
						Salida = StringSalidaFinal.toString();
						
						}
				if (!Salida.isEmpty())
					CodigoHTML.append(Salida);
					
			}
			
			CodigoHTML.append("</ul>");
			CodigoHTML.append("<br>");
		}
		
		
	}

	

	private String processSTDatosYMeta(CompleteElementType completeST,
			CompleteDocuments completeDocuments) {
		StringBuffer StringSalida=new StringBuffer();

			
			StringBuffer Hijos=new StringBuffer();
			for (CompleteElementType hijo : completeST.getSons()) {
				
				String result2 = processSTResto(hijo, completeDocuments);
				
				if (!result2.isEmpty())
					Hijos.append(result2.toString());	
			}
			
			
			String HijosSalida = Hijos.toString();
			
			if (!HijosSalida.isEmpty())
			{
			
				String tipo = ReduceString(((CompleteElementType)completeST).getName());
				
				if (NameCSS.get(tipo)!=null&&NameCSS.get(tipo)!=completeST)
					tipo=CreateNameCSS(tipo,completeST);
				
				NameCSS.put(tipo,completeST);
				 
				String IDT=((CompleteElementType)completeST).getClavilenoid()+"";
				
				Integer IDT2 = StaticFuctionsHTMLOdA.getIDODAD(((CompleteElementType)completeST));
				if (IDT2!=null)
					IDT=IDT2+"";
				
				tipo=tipo+" N"+IDT;	
				
				
				
			StringSalida.append("<li><span class=\"_Type "+tipo+"\"> "+((CompleteElementType)completeST).getName()+":</span> </li>");
			StringSalida.append("<ul class=\"_List "+tipo+"\">");
			StringSalida.append(HijosSalida);
			StringSalida.append("</ul>");
			}
		


		return StringSalida.toString();
		
	}
	
	

	private String processSTResto(CompleteElementType completeST,
			CompleteDocuments completeDocuments) {
		StringBuffer StringSalida=new StringBuffer();
		boolean Vacio=true;
		boolean Visible=false;
		if (completeST instanceof CompleteElementType&&inList((CompleteElementType)completeST))
			{
			CompleteElement E=findElem(completeST,completeDocuments.getDescription());
			if (E!=null)
				{
				Vacio=false;
				if (E instanceof CompleteTextElement)
					{
					if (Administrador||StaticFuctionsHTMLOdA.getVisible((CompleteElementType)completeST))
						{
							String ValueText=((CompleteTextElement)E).getValue();
							if (StaticFuctionsHTMLOdA.isNumeric(E.getHastype()))
							{
								try {
									Double D=Double.parseDouble(ValueText);
									ValueText=D.toString();
									
									int p_ent= (int)D.intValue();
									 
									double p_dec= D - p_ent;
									
									if (p_dec==0)
										ValueText=Integer.toString(p_ent);
								
								} catch (Exception e2) {
								}
							}
							else
							if (StaticFuctionsHTMLOdA.isDate(E.getHastype()))
							{
								try {

									
									Date fecha = null;
									//yyyy-MM-dd HH:mm:ss
									try {
										SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
										fecha = formatoDelTexto.parse(ValueText);
									} catch (Exception e) {
										//Nada
										fecha = null;
									}
									
									if (fecha==null)
										try {
											SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyy-MM-dd");
											fecha = formatoDelTexto.parse(ValueText);
										} catch (Exception e) {
											//Nada
											fecha = null;
										}
									
									if (fecha==null)
										try {
											SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyy-MM-dd HH:mm");
											fecha = formatoDelTexto.parse(ValueText);
										} catch (Exception e) {
											//Nada
											fecha = null;
										}
									
									if (fecha==null)
										try {
											SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyyMMdd");
											fecha = formatoDelTexto.parse(ValueText);
										} catch (Exception e) {
											//Nada
											fecha = null;
										}
									
									if (fecha==null)
										try {
											SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd/MM/yyyy");
											fecha = formatoDelTexto.parse(ValueText);
										} catch (Exception e) {
											//Nada
											fecha = null;
										}
									
									if (fecha==null)
										try {
											SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd/MM/yy");
											fecha = formatoDelTexto.parse(ValueText);
										} catch (Exception e) {
											//Nada
											fecha = null;
										}
									
									if (fecha!=null)
									{
									DateFormat df = new SimpleDateFormat ("dd/MM/yyyy");
									ValueText=df.format(fecha);	
									}
									else
									{
										CL.getLogLines().add("Problemas al parsear la fecha  " + ValueText + ",  solo formatos compatibles yyyy-MM-dd HH:mm:ss ó yyyy-MM-dd HH:mm ó yyyy-MM-dd ó yyyyMMdd ó dd/MM/yyyy ó dd/MM/yy");
									}
								} catch (Exception e2) {
								}
								
							}
							
							

							String tipo = ReduceString(((CompleteElementType)completeST).getName());
							
							if (NameCSS.get(tipo)!=null&&NameCSS.get(tipo)!=completeST)
								tipo=CreateNameCSS(tipo,completeST);
							
							NameCSS.put(tipo,completeST);
							 
							String IDT=((CompleteElementType)completeST).getClavilenoid()+"";
							
							Integer IDT2 = StaticFuctionsHTMLOdA.getIDODAD(((CompleteElementType)completeST));
							if (IDT2!=null)
								IDT=IDT2+"";
							
							tipo=tipo+" N"+IDT;
							
							
							StringSalida.append("<li><span class=\"_Type "+tipo+"\"> "+((CompleteElementType)completeST).getName()+":</span> <span class=\"_Value "+tipo+"V \">"+ValueText+"</span></li>");
							StringSalida.append("<ul class=\"_List "+tipo+"\">");
							Visible=true;
						}
					}
				else 
					Vacio=true;
				
				}
			else
				Vacio=true;
			
			StringBuffer Hijos=new StringBuffer();
			for (CompleteElementType hijo : completeST.getSons()) {
				
				String result2 = processSTResto(hijo, completeDocuments);
				
				if (!result2.isEmpty())
					Hijos.append(result2.toString());	
			}
			
			
			String HijosSalida = Hijos.toString();
			
			if (!HijosSalida.isEmpty()&&Vacio&&(Administrador||StaticFuctionsHTMLOdA.getVisible((CompleteElementType)completeST)))
			{
				

				String tipo = ReduceString(((CompleteElementType)completeST).getName());
				
				if (NameCSS.get(tipo)!=null&&NameCSS.get(tipo)!=completeST)
					tipo=CreateNameCSS(tipo,completeST);
				
				NameCSS.put(tipo,completeST);
				 
				String IDT=((CompleteElementType)completeST).getClavilenoid()+"";
				
				Integer IDT2 = StaticFuctionsHTMLOdA.getIDODAD(((CompleteElementType)completeST));
				if (IDT2!=null)
					IDT=IDT2+"";
				
				tipo=tipo+" N"+IDT;
				
				
			StringSalida.append("<li> <span class=\"_Type "+tipo+"\"> "+((CompleteElementType)completeST).getName()+":</span> </li>");
			StringSalida.append("<ul class=\"_List "+tipo+"\">");
			Visible=true;
			}
		
		if (!HijosSalida.isEmpty())
			StringSalida.append(HijosSalida);
			
		if (Visible)
			StringSalida.append("</ul>");
		
			}

		return StringSalida.toString();
		
	}
	
	
	
	private boolean inList(CompleteElementType completeST) {
		if (AdministradorListaStructura.isEmpty())
		return true;
		else
			{
			Integer IDOdA=StaticFuctionsHTMLOdA.getIDODAD(completeST);
			if (IDOdA==null)
				return false;
			else
			return AdministradorListaStructura.contains(new Long(IDOdA));
			}
	}

	private String processSTRecursos(CompleteElementType completeST,
			CompleteDocuments completeDocuments) {
		StringBuffer StringSalida=new StringBuffer();

			
			String tipo = ReduceString(((CompleteElementType)completeST).getName());
			
			if (NameCSS.get(tipo)!=null&&NameCSS.get(tipo)!=completeST)
				tipo=CreateNameCSS(tipo,completeST);
			
			NameCSS.put(tipo,completeST);
			 
			String IDT=((CompleteElementType)completeST).getClavilenoid()+"";
			
			Integer IDT2 = StaticFuctionsHTMLOdA.getIDODAD(((CompleteElementType)completeST));
			if (IDT2!=null)
				IDT=IDT2+"";
			
			tipo=tipo+" N"+IDT;
			
			
			CompleteElement E=findElem(completeST,completeDocuments.getDescription());
			if (E!=null)
				{
				if (E instanceof CompleteLinkElement&&(StaticFuctionsHTMLOdA.getVisible(E)||Administrador||inList(completeDocuments)))
					{
					CompleteDocuments Linked=((CompleteLinkElement) E).getValue();
					
					File IconF=new File(SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid());
					IconF.mkdirs();
					
					String IconPath="";
					String Link="";
					String OVID=Linked.getClavilenoid()+"";
					
					boolean isAfile=false;
					
					if (StaticFuctionsHTMLOdA.isAVirtualObject(Linked,Salvar.getMetamodelGrammar()) )
						{
							for (CompleteElement elemetpos : Linked.getDescription()) {
								if (elemetpos instanceof CompleteTextElement&&elemetpos.getHastype() instanceof CompleteTextElementType&&StaticFuctionsHTMLOdA.isIDOV((CompleteTextElementType)elemetpos.getHastype()))
									OVID=((CompleteTextElement) elemetpos).getValue();
							}
						IconPath=StaticFunctionsHTML.calculaIconoString(Linked.getIcon());
						}
					else if (StaticFuctionsHTMLOdA.isAFile(Linked,Salvar.getMetamodelGrammar()) )
					{
						for (CompleteElement Elem : Linked.getDescription()) {
							if (Elem instanceof CompleteResourceElement&&Elem.getHastype() instanceof CompleteResourceElementType&&StaticFuctionsHTMLOdA.isFileFisico(Elem.getHastype()))
								{
								if (Elem instanceof CompleteResourceElementFile)
									{
									IconPath=StaticFunctionsHTML.calculaIconoString(((CompleteResourceElementFile) Elem).getValue().getPath());
									Link=((CompleteResourceElementFile) Elem).getValue().getPath();
									}
								if (Elem instanceof CompleteResourceElementURL)
									{
									IconPath=StaticFunctionsHTML.calculaIconoString(((CompleteResourceElementURL) Elem).getValue());
									Link=((CompleteResourceElementURL) Elem).getValue();
									}
								}
						}
						isAfile=true;
						
					}
					else if (StaticFuctionsHTMLOdA.isAURL(Linked,Salvar.getMetamodelGrammar()) )
					{
						for (CompleteElement Elem : Linked.getDescription()) {
							if (Elem instanceof CompleteResourceElement&&Elem.getHastype() instanceof CompleteResourceElementType&&StaticFuctionsHTMLOdA.isURI(Elem.getHastype()))
								{
								IconPath=StaticFunctionsHTML.calculaIconoStringURL();
								if (Elem instanceof CompleteResourceElementFile)
									{
									Link=((CompleteResourceElementFile) Elem).getValue().getPath();
									}
								if (Elem instanceof CompleteResourceElementURL)
									{
									Link=((CompleteResourceElementURL) Elem).getValue();
									}
								}
						}
					}
					
					String[] spliteStri=IconPath.split("/");
					String NameS = spliteStri[spliteStri.length-1];
					String Icon=SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid()+File.separator+NameS;
					
					try {
						URL url2 = new URL(IconPath);
						 URI uri2 = new URI(url2.getProtocol(), url2.getUserInfo(), url2.getHost(), url2.getPort(), url2.getPath(), url2.getQuery(), url2.getRef());
						 url2 = uri2.toURL();
						
						saveImage(url2, Icon);
					} catch (Exception e) {
						CL.getLogLines().add("Error en copia del icono con url ->>"+IconPath+" no encontrado o restringido");
					}
					
					
					String NameSL="";
					if (!Link.isEmpty()&&isAfile)
					{
					String[] spliteStriL=Link.split("/");
					NameSL = spliteStriL[spliteStriL.length-1];
					String IconL=SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid()+File.separator+NameSL;
					
					try {
						URL url2L = new URL(Link);
						 URI uri2L = new URI(url2L.getProtocol(), url2L.getUserInfo(), url2L.getHost(), url2L.getPort(), url2L.getPath(), url2L.getQuery(), url2L.getRef());
						 url2L = uri2L.toURL();
						
						saveImage(url2L, IconL);
					} catch (Exception e) {
						CL.getLogLines().add("Error en copia del archivo con url ->>"+Link+" no encontrado o restringido");
					}
					}
					
					
					
					int width= 150;
					int height=150;
					int widthmini= 150;
					int heightmini=150;
					int widthmaxi= 150;
					int heightmaxi=150;
					
					try {
						BufferedImage bimg = ImageIO.read(new File(SOURCE_FOLDER+File.separator+completeDocuments.getClavilenoid()+File.separator+NameS));
						width= bimg.getWidth();
						height= bimg.getHeight();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
					 widthmini= 150;
					 heightmini= (150*height)/width;
					 
					 widthmaxi= 600;
					 heightmaxi= (600*height)/width;
					
					 
					if (Link.isEmpty())
						StringSalida.append("<li> <img class=\"_ImagenOV "+tipo+"\" src=\""+completeDocuments.getClavilenoid()+File.separator+NameS+"\" onmouseover=\"this.width="+widthmaxi+";this.height="+heightmaxi
								+";\" onmouseout=\"this.width="+widthmini+";this.height="+heightmini+";\" width=\""+widthmini+"\" height=\""+heightmini+"\" alt=\""+
								IconPath+"\" /> <span class=\"Type LinkOD\">Objeto Digital: </span>"
								+"<span class=\"_OdAIDOV\">"+OVID+"</span> <span class=\""+tipo+"V _OdADescriptionRel _Value\">"+Linked.getDescriptionText()+"</span></li>");
					else
						if (isAfile)
							StringSalida.append("<li> <img class=\"_ImagenFile "+tipo+"\" src=\""+
						completeDocuments.getClavilenoid()+File.separator+NameS+
						"\" onmouseover=\"this.width="+widthmaxi+";this.height="+
						heightmaxi+";\" onmouseout=\"this.width="+widthmini+";this.height="+heightmini+";\" width=\""+widthmini+
						"\" height=\""+heightmini+"\" alt=\""+IconPath+"\" /> <a class=\"_LinkedRef  "+tipo+"V "+tipo+"A \" href=\""+completeDocuments.getClavilenoid()+File.separator+NameSL+"\" target=\"_blank\">"+
						NameSL+"</a></li>");
						else
							{
							if (!testLink(Link))
								Link="http://"+Link;
							
							StringSalida.append("<li> <img class=\"_URL "+tipo+"\" src=\""+completeDocuments.getClavilenoid()
							
									+File.separator+NameS+"\" onmouseover=\"this.width="+widthmaxi+";this.height="+heightmaxi+";\" onmouseout=\"this.width="+widthmini+
									";this.height="+heightmini+";\" width=\""+widthmini+"\" height=\""+heightmini+"\" alt=\""+IconPath+
									"\" /> <a class=\"_LinkedRef "+tipo+"V "+tipo+"A \" href=\""+Link+"\" target=\"_blank\">"+Link+"</a></li>");
							}
					
					StringBuffer Hijos=new StringBuffer();
					for (CompleteElementType hijo : completeST.getSons()) {
						
						String result2 = processSTResto(hijo, completeDocuments);
						
						if (!result2.isEmpty())
							Hijos.append(result2.toString());	
					}
					
					
					String HijosSalida = Hijos.toString();
					
				
				if (!HijosSalida.isEmpty())
					{
					
					
					
					StringSalida.append("<ul class=\"_List "+tipo+"\">");
					StringSalida.append(HijosSalida);
					StringSalida.append("</ul>");
					}
					
					}

				
				}

	

		return StringSalida.toString();
		
	}
	
	private ArrayList<CompleteElementType> findOdAElements(
			ArrayList<CompleteElementType> sons) {
		ArrayList<CompleteElementType> Salida=new ArrayList<CompleteElementType>();
		for (CompleteElementType hastype : sons) {
			if (hastype instanceof CompleteElementType&&StaticFuctionsHTMLOdA.isDatos((CompleteElementType)hastype))
				{
				Salida.add(hastype);
				break;
				}
				
		}
		for (CompleteElementType hastype : sons) {
			if (hastype instanceof CompleteElementType&&StaticFuctionsHTMLOdA.isMetaDatos((CompleteElementType)hastype))
				{
				Salida.add(hastype);
				break;
				}
				
		}
		return Salida;
	}

	public static void main(String[] args) {
		System.out.println(testLink("http://"));
		System.out.println(testLink("ftp://"));
		System.out.println(testLink("ftps://"));
		System.out.println(testLink("https://"));
		System.out.println(testLink("http://localhost/Oda"));
		System.out.println(testLink("http://localhost/Oda"));
		System.out.println(testLink("http://localhost:1000/Oda"));
		System.out.println(testLink("http://localhost:/Oda"));
		System.out.println(testLink("http://192.168.1.1:266/Oda"));
		System.out.println(!("http://localhost/Oda").endsWith("/"));
		System.out.println(!("http://localhost/oda-ref/").endsWith("/"));
	}
	
	
}
