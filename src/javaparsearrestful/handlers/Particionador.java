package javaparsearrestful.handlers;

import java.beans.Statement;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class Particionador extends AbstractHandler {
	// classes que não extendem nenhuma classe e nem implementam interfaces e são abstratas
	private List<IType> javaClassesAbstract;
	// classes que extendem alguma classe, mas não implementam interfaces e são abstratas
	private List<IType> javaChildrenClassesAbstract;
	// classes que não extendem alguma classe, mas que implementam interfaces e são abstratas
	private List<IType> javaClassesInterfacesAbstract;
	// classes que extendem classes e implementam interfaces e são abstratas
	private List<IType> javaChildrenClassesInterfacesAbstract;
	// classes que não extendem nenhuma classe e nem implementam interfaces
	private List<IType> javaClasses;
	// classes que extendem alguma classe, mas não implementam interfaces
	private List<IType> javaChildrenClasses;
	// classes que não extendem alguma classe, mas que implementam interfaces
	private List<IType> javaClassesInterfaces;
	// classes que extendem classes e implementam interfaces
	private List<IType> javaChildrenClassesInterfaces;
	// interfaces que não extendem de outras interfaces
	private List<IType> javaInterfaces;
	// interfaces que extendem de outras interfaces
	private List<IType> javaChildrenInterfaces;
	// managers do javaClasses
	private Map<IType,IType> managersJavaClasses;
	// resources do javaClasses
	private Map<IType,IType> resourcesJavaClasses;
	// nome do projeto
	private String projectName;
	/**
	 * The constructor.
	 */
	public Particionador() {
	}

	/**
	 * Após clicar em Particionador -> Processar, esse será a função que iniciará todo processo
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try{
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
	
			// recupera todos os projectos do workspace
			IProject[] projects = root.getProjects();
			
			System.out.println("Iniciando o particionamento...");
			System.out.println("");
			System.out.println("Escolha um dos projetos abaixo para ser realizado o processamento:");
			for(int i = 0; i < projects.length; i++){
				System.out.println((i+1)+") "+projects[i].getName());
			}
			
			System.out.println("");
			Scanner in = new Scanner(System.in);  
			
			String line = in.nextLine();
			Integer choosenProject = Integer.parseInt(line)-1;
			
			if(choosenProject >= projects.length || choosenProject < 0){
				System.out.println("Não existe projeto com o número escolhido... terminando");
	            return null;
			}
			
			// duplica projeto para nÃ£o perder dados do outro
			System.out.println("Duplicando projeto. Aguarde.");
			System.out.println(".");
			System.out.println("..");
			System.out.println("...");
			System.out.println("....");
			System.out.println(".....");
			System.out.println("......");
			IProject restfulProject = createRestfulProject(projects[choosenProject]);
			System.out.println("Projeto duplicado!");
			System.out.println(".");
			System.out.println("..");
			System.out.println("...");
			System.out.println("....");
			System.out.println(".....");
			System.out.println("......");
			
			// abre objeto do projeto Java para poder modificar
			IJavaProject javaProject = JavaCore.create(restfulProject);
			
			System.out.println("Inicializando variáveis. Aguarde.");
			System.out.println(".");
			System.out.println("..");
			System.out.println("...");
			System.out.println("....");
			System.out.println(".....");
			System.out.println("......");
			// inicia as variáveis globais
			initializeGlobalVariable(javaProject);
			System.out.println("Variáveis inicializadas!");
			System.out.println(".");
			System.out.println("..");
			System.out.println("...");
			System.out.println("....");
			System.out.println(".....");
			System.out.println("......");
			
			System.out.println("Criando os resource e manager base das classes que não extendem ou implementam classes ou interfaces!");
			System.out.println(".");
			System.out.println("..");
			System.out.println("...");
			System.out.println("....");
			System.out.println(".....");
			System.out.println("......");
			// cria os resource das classes que não extendem outra classe e não implementam interfaces
			createResourceSimpleType(javaProject);
			// cria os manager das classes que não extendem outra classe e não implementam interfaces
			createManagerSimpleType(javaProject);
			System.out.println("Processamento finalizado!");
			System.out.println(".");
			System.out.println("..");
			System.out.println("...");
			System.out.println("....");
			System.out.println(".....");
			System.out.println("......");
			
			System.out.println("Processando classes que não extendem classe e não implementam interface!");
			System.out.println(".");
			System.out.println("..");
			System.out.println("...");
			System.out.println("....");
			System.out.println(".....");
			System.out.println("......");
			// processa as classes que não contém extendem de nenhuma classe ou não implementam nenhum interface
			processSimpleType();
			System.out.println("Processamento finalizado!");
			System.out.println(".");
			System.out.println("..");
			System.out.println("...");
			System.out.println("....");
			System.out.println(".....");
			System.out.println("......");
			
		}
		catch(CoreException | MalformedTreeException | BadLocationException e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Cria os arquivos de resource base, linkados com os arquivos das Domain
	 * @param javaProject
	 * @throws CoreException 
	 */
	private void createResourceSimpleType(IJavaProject javaProject) throws CoreException {
		for(IType clazz : javaClasses){
			// salva um link entre a classe e o resource
			resourcesJavaClasses.put(clazz, createResourceJava(javaProject, clazz, clazz.getElementName()));
		}
		
		javaProject.getProject().getWorkspace().save(true,null);
	}
	
	/**
	 * Cria o arquivo DomainResource.java, contendo a base para o resource
	 * @param newProject
	 * @throws CoreException 
	 */
	private IType createResourceJava(IJavaProject javaProject, IType clazz, String domainName) throws CoreException {
		// cria o resource relativo ao clazz no package resource e retorna
		String resourceString = createResourceContentBasis(clazz, domainName);
		ICompilationUnit resourceCu = clazz.getPackageFragment().createCompilationUnit(domainName+"Resource.java", resourceString, true, null);
		clazz.getPackageFragment().makeConsistent(null);
		return resourceCu.getType(domainName+"Resource");
	}

	/**
	 * Cria a string contendo o corpo do resource 
	 * @param domainName
	 * @return String
	 */
	private String createResourceContentBasis(IType clazz, String domainName) {
		StringBuilder resourceBuilder = new StringBuilder();
		resourceBuilder.append("package "+clazz.getPackageFragment().getElementName()+";\n");
		resourceBuilder.append("\n");
		resourceBuilder.append("import java.util.Map;\n");
		resourceBuilder.append("\n");
		resourceBuilder.append("import "+clazz.getFullyQualifiedName()+";\n");
		resourceBuilder.append("\n");
		resourceBuilder.append("@javax.ws.rs.Path(\"/"+clazz.getElementName()+"\")\n");
		resourceBuilder.append("public class "+clazz.getElementName()+"Resource {\n");
		resourceBuilder.append("}\n");
		return resourceBuilder.toString();
	}

	/**
	 * Cria os arquivos de manager base, linkados com os arquivos das Domain
	 * @param javaProject
	 */
	private void createManagerSimpleType(IJavaProject javaProject) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Inicializa as variáveis globais
	 * @param javaProject
	 * @throws CoreException 
	 */
	private void initializeGlobalVariable(IJavaProject javaProject) throws CoreException {
		javaClasses = new ArrayList<IType>();
		javaChildrenClasses = new ArrayList<IType>();
		javaClassesInterfaces = new ArrayList<IType>();
		javaChildrenClassesInterfaces = new ArrayList<IType>();
		javaClassesAbstract = new ArrayList<IType>();
		javaChildrenClassesAbstract = new ArrayList<IType>();
		javaClassesInterfacesAbstract = new ArrayList<IType>();
		javaChildrenClassesInterfacesAbstract = new ArrayList<IType>();
		javaInterfaces = new ArrayList<IType>();
		javaChildrenInterfaces = new ArrayList<IType>();
		managersJavaClasses = new HashMap<IType, IType>();
		resourcesJavaClasses = new HashMap<IType, IType>();
		
		// itera sobre os pacotes e arquivos, para inicializar as variáveis contendo as classes java e interfaces do projeto
		iterateJavaFiles(javaProject);
	}
	
	/** 
	 * Itera sobre os pacotes existentes no projeto, para atualizar as variáveis necessárias
	 * @param javaProject
	 * @throws CoreException
	 */
	private void iterateJavaFiles(IJavaProject javaProject) throws CoreException {
		IPackageFragment[] packages = javaProject.getPackageFragments();
		// for each package, discover files.java
		// Package fragments include all packages in the
		// classpath
		// We will only look at the package from the source
		// folder
		// K_BINARY would include also included JARS, e.g.
		// rt.jar
		for (IPackageFragment mypackage : packages) {
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				// enter into all java files
				enterJavaFiles(mypackage);
			}
		}
	}
	
	/**
	 * Itera sobre todas as classes de um pacote, e verifica se pertencem a um dos seguintes grupos:
	 * 1) classes que não extendem outra classe e não implementam nenhuma interface
	 * 2) classes que não extendem outra classe, mas implementam interface
	 * 3) classes que extendem outra classe, mas não implementam nenhuma interface
	 * 4) classes que entendem outra classe e implementam interface
	 * 5) interfaces que não extendem outras interfaces
	 * 6) interfaces que extendem outras interfaces 
	 * @param mypackage
	 * @throws JavaModelException
	 */
	private void enterJavaFiles(IPackageFragment mypackage) throws JavaModelException {

		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			// enter into all classes
			for (IType type : unit.getAllTypes()) {
				Boolean extendsClass = type.getSuperclassName() == null? false:true;
				Boolean implementsInterface = type.getSuperInterfaceNames().length == 0? false:true;
				// se for classe 
				if(type.isClass()){
					// se extender classe
					if(extendsClass){
						// se implementa interface
						if(implementsInterface){
							if(Flags.isAbstract(type.getFlags()))
								javaChildrenClassesInterfacesAbstract.add(type);
							else
								javaChildrenClassesInterfaces.add(type);
						}
						// se não implementa interfaces
						else{
							if(Flags.isAbstract(type.getFlags()))
								javaChildrenClassesAbstract.add(type);
							else
								javaChildrenClasses.add(type);
						}
					}
					// se não extende classe
					else{
						// se implementa interface
						if(implementsInterface){
							if(Flags.isAbstract(type.getFlags()))
								javaClassesInterfacesAbstract.add(type);
							else
								javaClassesInterfaces.add(type);
						}
						// se não implementa interfaces
						else{
							if(Flags.isAbstract(type.getFlags()))
								javaClassesAbstract.add(type);
							else
								javaClasses.add(type);
						}
					}
				}
				else{
					if(type.isInterface()){
						// se implementa interface
						if(implementsInterface){
							javaChildrenInterfaces.add(type);
						}
						// se não implementa interfaces
						else{
							javaInterfaces.add(type);
						}
					}
				}
			}
		}
	}

	/**
	 * Cria um projeto cópia, para serem realizadas as alterações, sem perder os dados do projeto original
	 * @param project
	 * @return IProject
	 * @throws CoreException
	 */
	private IProject createRestfulProject(IProject project) throws CoreException {
		if (!project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
			System.out.println("Esse não é um projeto Java. Terminando.");
			System.exit(0);
		}
		
		projectName = project.getName() + "RestfulProject";

		// faz a cÃ³pia
		IProjectDescription projectDescription = project.getDescription();
		projectDescription.setName(projectName);
		URI uri = URI.create(project.getLocationURI().toString()
				+ "RestfulProject");
		projectDescription.setLocationURI(uri);
		
		String [] natures = {"org.eclipse.jem.workbench.JavaEMFNature</nature>",
		                  "org.eclipse.wst.common.modulecore.ModuleCoreNature",
		                  "org.eclipse.jdt.core.javanature",
		                  "org.eclipse.m2e.core.maven2Nature",
		                  "org.eclipse.wst.common.project.facet.core.nature",
		                  "org.eclipse.wst.jsdt.core.jsNature"};
		projectDescription.setNatureIds(natures);
		
		project.copy(projectDescription, true, null);

		// recupera o projeto para ser utilizado
		IWorkspaceRoot root = project.getWorkspace().getRoot();
		IProject newProject = root.getProject(projectName);

		// cria o arquivo pom.xml para o projeto
		createPom(newProject);
		
		// cria o arquivo web.xml para o projeto
		createWebXml(newProject);
		
		// cria o arquivo RestfulJacksonProvider.java
		createRestfulProvider(newProject);
		
		// cria o arquivo Utils.java
		createUtilJava(newProject);
		
		newProject.open(null);

		return newProject;
	}
	
	/**
	 * Cria o arquivo Utils.java, contendo funções úteis para o novo projeto
	 * @param newProject
	 * @throws CoreException 
	 */
	private void createUtilJava(IProject newProject) throws CoreException {
		IFolder srcFolder = newProject.getFolder("src");
		if(!srcFolder.exists())
			srcFolder.create(IFolder.FORCE, true, null);
		
		IFolder mainFolder = srcFolder.getFolder("main");
		if(!mainFolder.exists())
			mainFolder.create(IFolder.FORCE, true, null);
		
		IFolder javaFolder = mainFolder.getFolder("java");
		if(!javaFolder.exists())
			javaFolder.create(IFolder.FORCE, true, null);
		
		IFolder utilsFolder = javaFolder.getFolder("utils");
		if(!utilsFolder.exists())
			utilsFolder.create(IFolder.FORCE, true, null);
		
		IFile utils = utilsFolder.getFile("Utils.java");
		String utilsString = new String();
		utilsString = generateUtilsString();
		InputStream providerIS = new ByteArrayInputStream(utilsString.getBytes());
		utils.create(providerIS, IFile.FORCE, null);
		
	}

	/**
	 * Retorna string para geração do arquivo Utils
	 * @return String
	 */
	private String generateUtilsString() {
		StringBuilder utilsString = new StringBuilder();
		utilsString.append("package main.java.utils;\n");
		utilsString.append("\n");
		utilsString.append("import java.util.Arrays;\n");
		utilsString.append("import java.util.Map;\n");
		utilsString.append("\n");
		utilsString.append("import com.google.gson.Gson;\n");
		utilsString.append("\n");
		utilsString.append("public class Utils {\n");
		utilsString.append("	private static final Gson gson = new Gson();\n");
		utilsString.append("\n");
		utilsString.append("	public static Boolean checkParameters(Map<String, Object> data, String ... keys){\n");
		utilsString.append("		return data.keySet().containsAll(Arrays.asList(keys));\n");
		utilsString.append("	}\n");
		utilsString.append("	public static Boolean isJSONValid(String jsonInString){\n");
		utilsString.append("		try {\n");
		utilsString.append("	          gson.fromJson(jsonInString, Object.class);\n");
		utilsString.append("	          return true;\n");
		utilsString.append("	      } catch(com.google.gson.JsonSyntaxException ex) { \n");
		utilsString.append("	          return false;\n");
		utilsString.append("	      }\n");
		utilsString.append("	}\n");
		utilsString.append("}\n");
		return utilsString.toString();
	}

	/**
	 * Cria o mapper para transformar objeto em json e json em objeto, no arquivo RestfulJacksonJsonProvider.java
	 * @param newProject
	 * @throws CoreException 
	 */
	private void createRestfulProvider(IProject newProject) throws CoreException {
		IFolder srcFolder = newProject.getFolder("src");
		if(!srcFolder.exists())
			srcFolder.create(IFolder.FORCE, true, null);
		
		IFolder mainFolder = srcFolder.getFolder("main");
		if(!mainFolder.exists())
			mainFolder.create(IFolder.FORCE, true, null);
		
		IFolder javaFolder = mainFolder.getFolder("java");
		if(!javaFolder.exists())
			javaFolder.create(IFolder.FORCE, true, null);
		
		IFolder providerFolder = javaFolder.getFolder("provider");
		if(!providerFolder.exists())
			providerFolder.create(IFolder.FORCE, true, null);
		
		IFile provider = providerFolder.getFile("RestfulJacksonJsonProvider.java");
		String providerString = new String();
		providerString = generateProviderString();
		InputStream providerIS = new ByteArrayInputStream(providerString.getBytes());
		provider.create(providerIS, IFile.FORCE, null);
	}

	/**
	 * Retorna string para geração do mapper
	 * @return String
	 */
	private String generateProviderString() {
		StringBuilder providerString = new StringBuilder();
		providerString.append("package main.java.provider;\n");
		providerString.append("\n");
		providerString.append("import javax.ws.rs.ext.ContextResolver;\n");
		providerString.append("import javax.ws.rs.ext.Provider;\n");
		providerString.append("import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;\n");
		providerString.append("import com.fasterxml.jackson.annotation.PropertyAccessor;\n");
		providerString.append("import com.fasterxml.jackson.databind.ObjectMapper;\n");
		providerString.append("\n");
		providerString.append("@Provider\n");
		providerString.append("public class RestfulJacksonJsonProvider implements ContextResolver<ObjectMapper> {\n");
		providerString.append("    private static final ObjectMapper MAPPER = new ObjectMapper();\n");
		providerString.append("\n");
		providerString.append("    public RestfulJacksonJsonProvider() {\n");
		providerString.append("    	MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);\n");
		providerString.append("        System.out.println(\"Instantiate RestfulJacksonJsonProvider\");");
		providerString.append("    }\n");
		providerString.append("\n");
		providerString.append("    @Override\n");
		providerString.append("    public ObjectMapper getContext(Class<?> type) {\n");
		providerString.append("        System.out.println(\"RestfulJacksonJsonProvider.getContext() called with type: \"+type);");
		providerString.append("        return MAPPER;\n");
		providerString.append("    }\n");
		providerString.append("}\n");
		return providerString.toString();
	}

	/**
	 * Cria o arquivo web.xml, para ser utilizado como um webservice
	 * @param newProject
	 * @throws CoreException 
	 */
	private void createWebXml(IProject newProject) throws CoreException {
		IFolder srcFolder = newProject.getFolder("src");
		if(!srcFolder.exists())
			srcFolder.create(IFolder.FORCE, true, null);
		
		IFolder mainFolder = srcFolder.getFolder("main");
		if(!mainFolder.exists())
			mainFolder.create(IFolder.FORCE, true, null);
		
		IFolder webappFolder = mainFolder.getFolder("webapp");
		if(!webappFolder.exists())
			webappFolder.create(IFolder.FORCE, true, null);
		
		IFolder webinfFolder = webappFolder.getFolder("WEB-INF");
		if(!webinfFolder.exists())
			webinfFolder.create(IFolder.FORCE, true, null);
		
		IFile webXml = webinfFolder.getFile("web.xml");
		String webXmlString = new String();
		webXmlString = generateWebXmlString();
		InputStream webXmlIS = new ByteArrayInputStream(webXmlString.getBytes());
		webXml.create(webXmlIS, IFile.FORCE, null);
		
	}

	/**
	 * Retorna uma String, contendo todo o arquivo do web.xml em String
	 * @return String
	 */
	private String generateWebXmlString() {
		StringBuilder webXmlString = new StringBuilder();
		webXmlString.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		webXmlString.append("<web-app xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" \n");
		webXmlString.append("    xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\" \n");
		webXmlString.append("    id=\"WebApp_ID\" version=\"3.1\">\n");
		webXmlString.append("  <session-config>\n");
		webXmlString.append("    <session-timeout>30</session-timeout>\n");
		webXmlString.append("    <cookie-config>\n");
		webXmlString.append("      <name>SESSIONID</name>\n");
		webXmlString.append("    </cookie-config>\n");
		webXmlString.append("  </session-config>\n");
		webXmlString.append("</web-app>\n");
		return webXmlString.toString();
	}

	/**
	 * Cria o arquivo pom.xml para o novo projeto, para realizar as importações relativas ao Jackson e Jersey
	 * @param newProject
	 * @throws CoreException
	 */
	private void createPom(IProject newProject) throws CoreException{
		IFile pom = newProject.getFile("pom.xml");
		String pomString = new String();
		pomString = generatePomString();
		InputStream pomIS = new ByteArrayInputStream(pomString.getBytes());
		pom.create(pomIS, IFile.FORCE, null);
	}
	
	/**
	 * Retorna uma String, contendo todo o arquivo do pom.xml em String
	 * @return String
	 */
	private String generatePomString(){
		StringBuilder pomString = new StringBuilder();
		pomString.append("<?xml version=\"1.0\"?>\n");
		pomString.append("<project\n");
		pomString.append("	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\"\n");
		pomString.append("	xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
		pomString.append("	<modelVersion>4.0.0</modelVersion>\n");
		pomString.append("	<groupId>br.ufscar.doutorado</groupId>\n");
		pomString.append("	<artifactId>"+projectName+"</artifactId>\n");
		pomString.append("	<version>1.0-SNAPSHOT</version>\n");
		pomString.append("	<name>"+projectName+"</name>\n");
		pomString.append("	<packaging>war</packaging>\n");
		pomString.append("	<properties>\n");
		pomString.append("		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n");
		pomString.append("		<endorsed.dir>${project.build.directory}/endorsed</endorsed.dir>\n");
		pomString.append("		<version.jdk>1.8</version.jdk>  <!-- 1.7 for JDK 7 -->\n");
		pomString.append("		<version.mvn.compiler>3.2</version.mvn.compiler>\n");
		pomString.append("		<version.mvn.war.plugin>2.6</version.mvn.war.plugin>\n");
		pomString.append("		<version.jersey>2.15</version.jersey>\n");
		pomString.append("		<version.servlet.api>3.1.0</version.servlet.api>  <!-- use 3.0.1 for Tomcat 7 or Java EE 6 (i.e. Glassfish 3.x) -->\n");
		pomString.append("	</properties>\n");
		pomString.append("	<repositories>\n");
		pomString.append("		<repository>\n");
		pomString.append("			<id>java.net-Public</id>\n");
		pomString.append("			<name>Maven Java Net Snapshots and Releases</name>\n");
		pomString.append("			<url>https://maven.java.net/content/groups/public/</url>\n");
		pomString.append("			<layout>default</layout>\n");
		pomString.append("		</repository>\n");
		pomString.append("		<repository>\n");
		pomString.append("			<id>Central</id>\n");
		pomString.append("			<name>Maven Repository</name>\n");
		pomString.append("			<url>http://repo1.maven.org/maven2</url>\n");
		pomString.append("			<layout>default</layout>\n");
		pomString.append("		</repository>\n");
		pomString.append("		<repository>\n");
		pomString.append("			<id>central</id>\n");
		pomString.append("			<name>Central Repository</name>\n");
		pomString.append("			<url>http://repo.maven.apache.org/maven2</url>\n");
		pomString.append("			<layout>default</layout>\n");
		pomString.append("			<snapshots>\n");
		pomString.append("				<enabled>false</enabled>\n");
		pomString.append("			</snapshots>\n");
		pomString.append("		</repository>\n");
		pomString.append("		<repository>\n");
		pomString.append("			<url>http://download.eclipse.org/rt/eclipselink/maven.repo/</url>\n");
		pomString.append("			<id>eclipselink</id>\n");
		pomString.append("			<layout>default</layout>\n");
		pomString.append("			<name>Repository for library EclipseLink (JPA 2.0)</name>\n");
		pomString.append("		</repository>\n");
		pomString.append("	</repositories>\n");
		pomString.append("	<dependencies>\n");
		pomString.append("		<!-- https://mvnrepository.com/artifact/com.google.code.gson/gson -->\n");
		pomString.append("		<dependency>\n");
		pomString.append("			<groupId>com.google.code.gson</groupId>\n");
		pomString.append("			<artifactId>gson</artifactId>\n");
		pomString.append("			<version>2.8.2</version>\n");
		pomString.append("		</dependency>\n");
		pomString.append("		<dependency>\n");
		pomString.append("			<groupId>javax.servlet</groupId>\n");
		pomString.append("			<artifactId>javax.servlet-api</artifactId>\n");
		pomString.append("			<version>${version.servlet.api}</version>\n");
		pomString.append("			<scope>provided</scope>\n");
		pomString.append("		</dependency>\n");
		pomString.append("		<dependency>\n");
		pomString.append("			<groupId>org.apache.httpcomponents</groupId>\n");
		pomString.append("			<artifactId>httpclient</artifactId>\n");
		pomString.append("			<version>4.1.1</version>\n");
		pomString.append("		</dependency>\n");
		pomString.append("		<!-- Jersey -->\n");
		pomString.append("		<dependency>\n");
		pomString.append("			<groupId>org.glassfish.jersey.containers</groupId>\n");
		pomString.append("			<artifactId>jersey-container-servlet</artifactId>\n");
		pomString.append("			<version>${version.jersey}</version>\n");
		pomString.append("		</dependency>\n");
		pomString.append("		<dependency>\n");
		pomString.append("			<groupId>org.glassfish.jersey.media</groupId>\n");
		pomString.append("			<artifactId>jersey-media-json-jackson</artifactId>\n");
		pomString.append("			<version>${version.jersey}</version>\n");
		pomString.append("		</dependency>\n");
		pomString.append("		<dependency>\n");
		pomString.append("			<groupId>org.glassfish.jersey.media</groupId>\n");
		pomString.append("			<artifactId>jersey-media-json-processing</artifactId>\n");
		pomString.append("			<version>${version.jersey}</version>\n");
		pomString.append("		</dependency>\n");
		pomString.append("		<dependency>\n");
		pomString.append("			<groupId>org.glassfish.jersey.media</groupId>\n");
		pomString.append("			<artifactId>jersey-media-multipart</artifactId>\n");
		pomString.append("			<version>${version.jersey}</version>\n");
		pomString.append("		</dependency>\n");
		pomString.append("		<dependency>\n");
		pomString.append("			<groupId>org.glassfish.jersey.media</groupId>\n");
		pomString.append("			<artifactId>jersey-media-sse</artifactId>\n");
		pomString.append("			<version>${version.jersey}</version>\n");
		pomString.append("		</dependency>\n");
		pomString.append("		<dependency>\n");
		pomString.append("			<groupId>org.slf4j</groupId>\n");
		pomString.append("			<artifactId>slf4j-api</artifactId>\n");
		pomString.append("			<version>1.7.25</version>\n");
		pomString.append("		</dependency>\n");
		pomString.append("	</dependencies>\n");
		pomString.append("	<build>\n");
		pomString.append("		<plugins>\n");
		pomString.append("			<plugin>\n");
		pomString.append("				<groupId>org.apache.maven.plugins</groupId>\n");
		pomString.append("				<artifactId>maven-compiler-plugin</artifactId>\n");
		pomString.append("				<version>${version.mvn.compiler}</version>\n");
		pomString.append("				<configuration>\n");
		pomString.append("					<source>${version.jdk}</source>\n");
		pomString.append("					<target>${version.jdk}</target>\n");
		pomString.append("				</configuration>\n");
		pomString.append("			</plugin>\n");
		pomString.append("			<plugin>\n");
		pomString.append("				<groupId>org.apache.maven.plugins</groupId>\n");
		pomString.append("				<version>${version.mvn.war.plugin}</version>\n");
		pomString.append("				<artifactId>maven-war-plugin</artifactId>\n");
		pomString.append("				<configuration>\n");
		pomString.append("					<failOnMissingWebXml>true</failOnMissingWebXml>\n");
		pomString.append("					<archive>\n");
		pomString.append("						<addMavenDescriptor>false</addMavenDescriptor>\n");
		pomString.append("					</archive>\n");
		pomString.append("				</configuration>\n");
		pomString.append("			</plugin>\n");
		pomString.append("		</plugins>\n");
		pomString.append("	</build>\n");
		pomString.append("</project>\n");
		return pomString.toString();
	}
	
	/**
	 * Função para recuperar o TypeDeclaration de uma compilation unit
	 * @param cuType
	 * @return TypeDeclaration
	 */
	private TypeDeclaration getTypeDeclaration(CompilationUnit cuType){
		TypeDeclaration tdType = (TypeDeclaration) cuType.types().get(0);
		
		return tdType;
	}
	
	/**
	 * Recupera uma CompilationUnit de um type (classe, interface ou enum), para acessar a árvore AST
	 * @param type
	 * @return CompilationUnit
	 */
	private CompilationUnit getCompilationUnit(IType type){
		ASTParser parserType = ASTParser.newParser(AST.JLS8);
		parserType.setResolveBindings(true);
		parserType.setKind(ASTParser.K_COMPILATION_UNIT);
		parserType.setSource(type.getCompilationUnit());
		CompilationUnit cuType = (CompilationUnit) parserType.createAST(null);
		cuType.recordModifications();
		return cuType;
	}
	
	/**
	 * Recupera um Document gerado a partir de uma CompilationUnit
	 * @param type
	 * @return Document
	 * @throws JavaModelException
	 */
	private Document getDocumentCompilationUnit(IType type) throws JavaModelException{
		Document documentCompilationUnit = new Document(type.getCompilationUnit().getSource());
		return documentCompilationUnit;
	}
	
	/**
	 * Atualiza o arquivo físico da classe
	 * @param cu
	 * @param type
	 * @param document
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 * @throws JavaModelException
	 */
	private void saveUpdatesCompilationUnit(CompilationUnit cu, IType type, Document document) throws MalformedTreeException, BadLocationException, JavaModelException{
		TextEdit editClasse = cu.rewrite(document, type.getCompilationUnit().getJavaProject().getOptions(true));
		editClasse.apply(document);
		String newSourceClasse = document.get();
		type.getCompilationUnit().getBuffer().setContents(newSourceClasse);
		type.getCompilationUnit().getBuffer().save(null, true);
	}
	
	/**
	 * Itera sobre todas as classes pertencentes ao projeto, adicionando anotações quando necessário
	 * e criando o resource para a classe  
	 * @throws JavaModelException 
	 * @throws BadLocationException 
	 * @throws MalformedTreeException 
	 */
	private void processSimpleType() throws JavaModelException, MalformedTreeException, BadLocationException{
		for(IType type : javaClasses){
			Document javaDocument = getDocumentCompilationUnit(type);
			CompilationUnit cuClazz = getCompilationUnit(type);
			TypeDeclaration tdClazz = getTypeDeclaration(cuClazz);
			
			// atualiza o simple type alterando as funções necessárias e criando manafger e resource
			updateSimpleType(tdClazz, type);
			
			// Adiciona os imports necessários para a classe
			List<String> imports = new ArrayList<String>();
			imports.add("com.fasterxml.jackson.annotation.JsonIgnore");
			addImportsToType(cuClazz, imports);
			
			// salva as alterações realizadas na classe
			saveUpdatesCompilationUnit(cuClazz, type, javaDocument);
			
		}
	}
	
	/**
	 * Adiciona uma lista de imports em um type
	 * @param type
	 * @param imports
	 */
	private void addImportsToType(CompilationUnit cuType, List<String> imports) {
		for(String importType : imports){
			ImportDeclaration impD = cuType.getAST().newImportDeclaration();
			impD.setName(cuType.getAST().newName(importType));
			cuType.imports().add(impD);
		}
	}

	/**
	 * Imprime dados relativos às variáveis globais que foram criadas durante o desenvolvimento, para debug
	 * @throws JavaModelException
	 */
	private void printGlobalVariableData() throws JavaModelException {
		System.out.println("Quantidade de classes: "+(javaClasses.size()+javaChildrenClasses.size()+
				javaChildrenClassesInterfaces.size()+javaClassesInterfaces.size()));
		
		System.out.println(".");
		System.out.println("..");
		System.out.println("...");
		System.out.println("....");
		System.out.println(".....");
		System.out.println("......");
		System.out.println("Classes que não extendem classe e não implementam interfaces: "+javaClasses.size());
		for(IType type : javaClasses){
			System.out.println("Classe: "+type.getFullyQualifiedName()+", superClasse: "+type.getSuperclassName()+", interfaces: "+type.getSuperInterfaceNames().length);;
		}
		
		System.out.println(".");
		System.out.println("..");
		System.out.println("...");
		System.out.println("....");
		System.out.println(".....");
		System.out.println("......");
		System.out.println("Classes que extendem classe e não implementam interfaces: "+javaChildrenClasses.size());
		for(IType type : javaChildrenClasses){
			System.out.println("Classe: "+type.getFullyQualifiedName()+", superClasse: "+type.getSuperclassName()+", interfaces: "+type.getSuperInterfaceNames().length);
		}
		
		System.out.println(".");
		System.out.println("..");
		System.out.println("...");
		System.out.println("....");
		System.out.println(".....");
		System.out.println("......");
		System.out.println("Classes que não extendem classe e implementam interfaces: "+javaClassesInterfaces.size());
		for(IType type : javaClassesInterfaces){
			System.out.println("Classe: "+type.getFullyQualifiedName()+", superClasse: "+type.getSuperclassName()+", interfaces: "+type.getSuperInterfaceNames().length);
		}
		
		System.out.println(".");
		System.out.println("..");
		System.out.println("...");
		System.out.println("....");
		System.out.println(".....");
		System.out.println("......");
		System.out.println("Classes que extendem classe e não implementam interfaces: "+javaChildrenClassesInterfaces.size());
		for(IType type : javaChildrenClassesInterfaces){
			System.out.println("Classe: "+type.getFullyQualifiedName()+", superClasse: "+type.getSuperclassName()+", interfaces: "+type.getSuperInterfaceNames().length);
		}
	}
	
	/**
	 * Atualiza a classe simples. Setar: @JsonIgnore nas funções
	 * @param type
	 * @throws BadLocationException 
	 * @throws JavaModelException 
	 * @throws MalformedTreeException 
	 */
	private void updateSimpleType(TypeDeclaration type, IType clazz) throws MalformedTreeException, JavaModelException, BadLocationException{
		// Atualiza os construtores com parâmetros, adicionando as anotações necessárias
//		updateConstructor(type);
		// Atualiza as funções, adicionando @JsonIgnore para que não ocorra problemas de getNomeDeCampo
		
		// atualiza resource respectivo para a classe
		updateResourceSimpleType(type, clazz);
		
		updateMethods(type);
	}
	
	/** 
	 * Atualiza os métodos para servirem às chamadas restful
	 * @param type
	 */
	private void updateMethods(TypeDeclaration type) {
		// retorna métodos construtores
		List<MethodDeclaration> simpleMethods = findSimpleMethods(type); 
				
		for(MethodDeclaration simpleMethod : simpleMethods){
			// adiciona anotação @JsonIgnore
			addJsonIgnoreSimpleMethod(simpleMethod);
		}
	}

	/**
	 * Adiciona a anotação @JsonIgnore no método
	 * @param simpleMethod
	 */
	private void addJsonIgnoreSimpleMethod(MethodDeclaration simpleMethod) {
		MarkerAnnotation annotation = simpleMethod.getAST().newMarkerAnnotation();
		annotation.setTypeName(simpleMethod.getAST().newName("JsonIgnore"));
		simpleMethod.modifiers().add(0, annotation);
	}

	/**
	 * Atualiza construtores
	 * @param type
	 */
//	private void updateConstructor(TypeDeclaration type) {
//		// retorna métodos construtores
//		List<MethodDeclaration> constructors = findConstructorMethods(type); 
//		
//		for(MethodDeclaration constructor : constructors){
//			// adiciona anotação @JsonCreator e @JsonProperties caso seja construtor com parâmetros
//			List<SingleVariableDeclaration> parameters = constructor.parameters();
//			if(!parameters.isEmpty()){
//				// adiciona @JsonCreator
//				addJsonCreatorConstructor(constructor);
//				
//				// adicionar @JsonProperties
//				// ...
//				
//			}
//		}
//	}

	/**
	 * Adiciona a anotação @JsonCreator no método
	 * @param constructor
	 */
//	private void addJsonCreatorConstructor(MethodDeclaration constructor) {
//		MarkerAnnotation annotation = constructor.getAST().newMarkerAnnotation();
//		annotation.setTypeName(constructor.getAST().newName("JsonCreator"));
//		constructor.modifiers().add(0, annotation);
//	}

	/**
	 * Itera pelas funções da classe e recupera aquelas que não são construtoras
	 * @param type 
	 * @return List<MethodDeclaration>
	 */
	private List<MethodDeclaration> findSimpleMethods(TypeDeclaration type) {
		List<MethodDeclaration> simpleMethods = new ArrayList<MethodDeclaration>();
		for(MethodDeclaration method : type.getMethods()){
			if(!method.isConstructor()){
				simpleMethods.add(method);
			}
		}
		return simpleMethods;
	}
	
	/**
	 * Itera pelas funções da classe e recupera aquelas que são construtoras
	 * @param type 
	 * @return List<MethodDeclaration>
	 */
	private List<MethodDeclaration> findConstructorMethods(TypeDeclaration type) {
		List<MethodDeclaration> constructors = new ArrayList<MethodDeclaration>();
		for(MethodDeclaration method : type.getMethods()){
			if(method.isConstructor()){
				constructors.add(method);
			}
		}
		return constructors;
	}

	/**
	 * Realiza a geração dos resources das classes que não extendem e nem implementam nenhuma classe ou interface 
	 * @param type
	 * @throws BadLocationException 
	 * @throws JavaModelException 
	 * @throws MalformedTreeException 
	 */
	private void updateResourceSimpleType(TypeDeclaration type, IType clazz) throws MalformedTreeException, JavaModelException, BadLocationException{
		// primeiro, cria a função newDominio, que verificará quais são os construtores e criará uma função
		// para retorno específico
		createNewDomainFunctionSimpleType(clazz, type);
		
		// depois, cria a função get
		createGetDomainFunctionSimpleType(clazz, type);
		
		// depois, cria as entradas respectivas para cada função
//		createMethodsEntriesSimpleType(clazz, type); // cria um metodo para todos os com mesmo nome - TODO: FALTA ARRUMAR O TIPO DE RETORNO SER OBJECT, E CRIAR UM CHECKTYPES PARA CHECAR O TIPO DE CADA PARAMETRO
		createMethodsResourceSimpleType(clazz, type); // cria um metodo para cada funcao, mesmo as com mesmo nome
		
		// por último, atualiza todos os imports
		updateResourceImports(clazz, type);
	}

	/**
	 * Cria um endpoint para cada funcao.
	 * @param clazz
	 * @param type
	 * @throws BadLocationException 
	 * @throws JavaModelException 
	 * @throws MalformedTreeException 
	 */
	private void createMethodsResourceSimpleType(IType clazz, TypeDeclaration type) throws MalformedTreeException, JavaModelException, BadLocationException {
		IType resourceDomain = resourcesJavaClasses.get(clazz);
		
		Document resourceDocument = getDocumentCompilationUnit(resourceDomain);
		
		CompilationUnit cuResource = getCompilationUnit(resourceDomain);
		TypeDeclaration tdResource = getTypeDeclaration(cuResource);
		
		// recupera os métodos em um Map<String,List<MethodDeclaration>>
		Map<String, List<MethodDeclaration>> mapMethods = findMethodsGroupedByNameNoPrivate(type);
		
		// verifica todos os metodos, para ir adicionando as chamadas, dentro da função
		// levando em consideração parâmetros
		
		for(Entry<String, List<MethodDeclaration>> entry : mapMethods.entrySet()){
			List<MethodDeclaration> methodsSameName = entry.getValue();
			Integer countMethod = 0;
			
			for(MethodDeclaration method : methodsSameName){
				// conta numero de vezes que o metodo existe, para criacao do endpoint e nome do metodo
				countMethod++;
				// são dois tipos de chamadas de método
				// existem os métodos 'normais'
				// existem os métodos 'static'
				// a 'chamada' é muito parecida, muda somente se você vai usar um objeto objeto.nomeMetodo()
				// para realizar a chamada, ou se você vai fazer a chama 'direto' Classe.nomeMetodo()
				// é necessário verificar se terá um tipo de retorno ou não
				// se tiver, dá um return, senão, não precisa
				
				//	@javax.ws.rs.Path("toString")
				//	@javax.ws.rs.POST
				//	@javax.ws.rs.Produces({javax.ws.rs.core.MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
				//	@javax.ws.rs.Consumes({javax.ws.rs.core.MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
				//	public String restCallToString(Map<String, Object> data) throws Exception{
				//		if(Utils.checkParameters(data, "restObjectBase", "properties", "name")){
				//		   	// se for static
				//			return Car.toString("properties", "name")
				//			// se não for static
				//			Car car = newCar((Map<String, Object>)data.get("restObjectBase"));
				//			return car.toString("properties", "name");
				//	    }
				//				
				//		if(Utils.checkParameters(data, "restObjectBase", "properties")){
				//	        // se for static
				//			return Car.toString("properties")
				//			// se não for static
				//			Car car = newCar((Map<String, Object>)data.get("restObjectBase"));
				//			return car.toString("properties");
				//	    }
				//	    
				//		throw new Exception("Ocorreu um erro na requisição. Número e nome dos parâmetros é inválido");
				//	}
				
				// adiciona o método newDomain
				MethodDeclaration newMethodRestCall = tdResource.getAST().newMethodDeclaration();
				// nome do método
				newMethodRestCall.setName(tdResource.getAST().newSimpleName("restCall"+method.getName().toString()+countMethod.toString()));
				
				// adiciona os annotation
				// @javax.ws.rs.Path("nomeChamada")
				SingleMemberAnnotation annotationPath = tdResource.getAST().newSingleMemberAnnotation();
				annotationPath.setTypeName(tdResource.getAST().newName("javax.ws.rs.Path"));
				StringLiteral getSLPath = tdResource.getAST().newStringLiteral();
				getSLPath.setLiteralValue(method.getName().toString()+countMethod.toString());
				annotationPath.setValue(getSLPath);
				newMethodRestCall.modifiers().add(0, annotationPath); 
			    // @javax.ws.rs.POST
				MarkerAnnotation annotationPost = tdResource.getAST().newMarkerAnnotation();
				annotationPost.setTypeName(tdResource.getAST().newName("javax.ws.rs.POST"));
				newMethodRestCall.modifiers().add(0, annotationPost);
			    // @javax.ws.rs.Produces({javax.ws.rs.core.MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
				SingleMemberAnnotation annotationProduces = tdResource.getAST().newSingleMemberAnnotation();
				annotationProduces.setTypeName(tdResource.getAST().newName("javax.ws.rs.Produces"));
				Name nameAppJSONProduces = tdResource.getAST().newName("javax.ws.rs.core.MediaType.APPLICATION_JSON");
				annotationProduces.setValue(nameAppJSONProduces);
				newMethodRestCall.modifiers().add(0, annotationProduces);
			    // @javax.ws.rs.Consumes({javax.ws.rs.core.MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
				SingleMemberAnnotation annotationConsumes = tdResource.getAST().newSingleMemberAnnotation();
				annotationConsumes.setTypeName(tdResource.getAST().newName("javax.ws.rs.Consumes"));
				Name nameAppJSONConsumes = tdResource.getAST().newName("javax.ws.rs.core.MediaType.APPLICATION_JSON");
				annotationConsumes.setValue(nameAppJSONConsumes);
				newMethodRestCall.modifiers().add(0, annotationConsumes);
				
				// atualiza o typeParameter da função
				for(TypeParameter tp : (List<TypeParameter>) method.typeParameters()){
					newMethodRestCall.typeParameters().add((TypeParameter) ASTNode.copySubtree(tdResource.getAST(), tp));
				}
				
				// flag do método
				newMethodRestCall.modifiers().addAll(tdResource.getAST().newModifiers(Modifier.PUBLIC));
				// tipo de retorno do método
				newMethodRestCall.setReturnType2(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Object")));
				// parâmetro do método
				// primeiro a declaração da variável
				SingleVariableDeclaration mapParameter = tdResource.getAST().newSingleVariableDeclaration();
				mapParameter.setName(tdResource.getAST().newSimpleName("data"));
				// tipo parametrizado Map<String,Object>
				ParameterizedType mapPT = tdResource.getAST().newParameterizedType(tdResource.getAST().newSimpleType(tdResource.getAST().newName("java.util.Map")));
				mapPT.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("String")));
				mapPT.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Object")));
				mapParameter.setType(mapPT);
				newMethodRestCall.parameters().add(mapParameter);
				// throw declaration na declaração
				Type throwException = tdResource.getAST().newSimpleType(tdResource.getAST().newSimpleName("Exception"));
				newMethodRestCall.thrownExceptionTypes().add(throwException);
				Type throwThrowable = tdResource.getAST().newSimpleType(tdResource.getAST().newSimpleName("Throwable"));
				newMethodRestCall.thrownExceptionTypes().add(throwThrowable);
				// corpo
				Block bodyNewMethodRestCall = tdResource.getAST().newBlock(); 
				// seta o corpo na função
				newMethodRestCall.setBody(bodyNewMethodRestCall);
				// adiciona a função na classe
				tdResource.bodyDeclarations().add(0, newMethodRestCall);
				
				// throw dentro do corpo
				ThrowStatement throwNewException = tdResource.getAST().newThrowStatement();
				ClassInstanceCreation cicThrowException = tdResource.getAST().newClassInstanceCreation();
				StringLiteral argumentException = tdResource.getAST().newStringLiteral();
				argumentException.setLiteralValue("Ocorreu um erro na requisição. Número e nome dos parâmetros é inválido");
				cicThrowException.arguments().add(argumentException);
				cicThrowException.setType(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Exception")));
				throwNewException.setExpression(cicThrowException);
				// adiciona o throw no corpo
				bodyNewMethodRestCall.statements().add(0, throwNewException);
				
				// inicia o if
				IfStatement ifMethod = tdResource.getAST().newIfStatement();
				
				// chamada para Utils.checkParameters(data, "param1", "param2", ...)
				MethodInvocation utilsCheckParametersInvocation = tdResource.getAST().newMethodInvocation();
				utilsCheckParametersInvocation.setName(tdResource.getAST().newSimpleName("checkParameters"));
				utilsCheckParametersInvocation.setExpression(tdResource.getAST().newName("main.java.utils.Utils"));
				// checkParameters(data..)
				SimpleName dataArgument = tdResource.getAST().newSimpleName("data");
				utilsCheckParametersInvocation.arguments().add(dataArgument);
				// checkParameters(data, "restObjectBase")
				StringLiteral restObjectBaseArgument = tdResource.getAST().newStringLiteral();
				restObjectBaseArgument.setLiteralValue("restObjectBase");
				utilsCheckParametersInvocation.arguments().add(restObjectBaseArgument);
				// comparação dentro do if
				ifMethod.setExpression(utilsCheckParametersInvocation);
				
				MethodInvocation invocationMethod = tdResource.getAST().newMethodInvocation();
				
				Block ifBlock = tdResource.getAST().newBlock();
				ifMethod.setThenStatement(ifBlock);
				
				// tem que verificar se tem retorno
				if(method.getReturnType2().toString().contentEquals(org.eclipse.jdt.core.dom.PrimitiveType.VOID.toString())){
					ifBlock.statements().add(tdResource.getAST().newExpressionStatement(invocationMethod));
				}
				else{
					// return methodInvocation
					ReturnStatement returnMethodInvocationRestObjectBase = tdResource.getAST().newReturnStatement();
					returnMethodInvocationRestObjectBase.setExpression(invocationMethod);
					ifBlock.statements().add(returnMethodInvocationRestObjectBase);
				}
				
				// // se for static
				// return Car.toString("properties", "name")
				if(Modifier.isStatic(method.getModifiers())){
					// Car.toString(...)
					// toString(...)
					invocationMethod.setName(tdResource.getAST().newSimpleName(method.getName().toString()));
					// Car.
					invocationMethod.setExpression(tdResource.getAST().newSimpleName(clazz.getElementName()));
				}
				// // se não for static
				// Car car = newCar((Map<String, Object>)data.get("restObjectBase"));
				// return car.toString("properties", "name");
				else{
					// newCar((Map<String, Object>)data.get("restObjectBase"))
					// data.get('restObjectBase')
					// restObjectBase
					StringLiteral variableStringLiteralRestObjectBase = tdResource.getAST().newStringLiteral();
					variableStringLiteralRestObjectBase.setLiteralValue("restObjectBase");
					// data.get
					MethodInvocation dataGetInvocRestObjectBase = tdResource.getAST().newMethodInvocation();
					dataGetInvocRestObjectBase.setName(tdResource.getAST().newSimpleName("get"));
					dataGetInvocRestObjectBase.setExpression(tdResource.getAST().newName("data"));
					dataGetInvocRestObjectBase.arguments().add(variableStringLiteralRestObjectBase);
					
					// (Map<String, Object>)data.get("restObjectBase")
					// tipo parametrizado Map<String,Object>
					ParameterizedType ptRestObjectBase = tdResource.getAST().newParameterizedType(tdResource.getAST().newSimpleType(tdResource.getAST().newName("java.util.Map")));
					ptRestObjectBase.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("String")));
					ptRestObjectBase.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Object")));
					// (Map<String, Object>)data.get("restObjectBase")
					CastExpression castArgument = tdResource.getAST().newCastExpression();
					castArgument.setType(ptRestObjectBase);
					castArgument.setExpression(dataGetInvocRestObjectBase);
					
					// newCar(..)
					MethodInvocation newDomainMethodInvocationRestObjectBase = tdResource.getAST().newMethodInvocation();
					newDomainMethodInvocationRestObjectBase.setName(tdResource.getAST().newSimpleName("new"+clazz.getElementName()));
					// newCar((Map<String, Object>)data.get("restObjectBase"))
					newDomainMethodInvocationRestObjectBase.arguments().add(castArgument);
					
					// Car car ...
					// car
					VariableDeclarationFragment vdfObj = tdResource.getAST().newVariableDeclarationFragment();
					vdfObj.setName(tdResource.getAST().newSimpleName("obj"));
					vdfObj.setInitializer(newDomainMethodInvocationRestObjectBase);
					// Car car = newCar((Map<String, Object>)data.get("restObjectBase"))
					VariableDeclarationStatement vdsObj = tdResource.getAST().newVariableDeclarationStatement(vdfObj);
					vdsObj.setType(tdResource.getAST().newSimpleType(tdResource.getAST().newName(clazz.getElementName())));
					
					// adicionando no corpo do if
					ifBlock.statements().add(0, vdsObj);
					
					// car.toString(...)
					// toString(...)
					invocationMethod.setName(tdResource.getAST().newSimpleName(method.getName().toString()));
					// car.
					invocationMethod.setExpression(tdResource.getAST().newSimpleName("obj"));
				}
				
				// itera pelos parâmetros para adicionar nos argumentos
				for(SingleVariableDeclaration svdMethod : (List<SingleVariableDeclaration>)method.parameters()){
					StringLiteral variableStringLiteralCheckParameter = tdResource.getAST().newStringLiteral();
					variableStringLiteralCheckParameter.setLiteralValue(svdMethod.getName().toString());
					utilsCheckParametersInvocation.arguments().add(variableStringLiteralCheckParameter);
					
					// data.get('arg')
					// arg
					StringLiteral variableStringLiteralReturn = tdResource.getAST().newStringLiteral();
					variableStringLiteralReturn.setLiteralValue(svdMethod.getName().toString());
					// data.get
					MethodInvocation dataGetInvoc = tdResource.getAST().newMethodInvocation();
					dataGetInvoc.setName(tdResource.getAST().newSimpleName("get"));
					dataGetInvoc.setExpression(tdResource.getAST().newName("data"));
					dataGetInvoc.arguments().add(variableStringLiteralReturn);
					
					// (Type)data.get('arg')
					Type returnTypeArg;
					if(svdMethod.getExtraDimensions() > 0){
						Type typeArg = (Type) ASTNode.copySubtree(tdResource.getAST(), svdMethod.getType());
						ArrayType arrayReturnTypeArg = tdResource.getAST().newArrayType(typeArg, svdMethod.getExtraDimensions());
						returnTypeArg = arrayReturnTypeArg;
					}
					else{
						returnTypeArg = (Type) ASTNode.copySubtree(tdResource.getAST(), svdMethod.getType());
					}
					
					CastExpression castArgument = tdResource.getAST().newCastExpression();
					castArgument.setType(returnTypeArg);
					castArgument.setExpression(dataGetInvoc);
					
					invocationMethod.arguments().add(castArgument);
				}
				
				bodyNewMethodRestCall.statements().add(0, ifMethod);
				
				
			}
		}
					
		// salva as alterações realizadas na classe
		saveUpdatesCompilationUnit(cuResource, resourceDomain, resourceDocument);
	}

	/**
	 * Função para iterar sobre os métodos públicos e protegidos da classe e criar as entradas para os mesmos
	 * para as chamadas REST
	 * @param clazz
	 * @param type
	 * @throws JavaModelException 
	 * @throws BadLocationException 
	 * @throws MalformedTreeException 
	 */
	private void createMethodsEntriesSimpleType(IType clazz, TypeDeclaration type) throws JavaModelException, MalformedTreeException, BadLocationException {
		IType resourceDomain = resourcesJavaClasses.get(clazz);
		
		Document resourceDocument = getDocumentCompilationUnit(resourceDomain);
		
		CompilationUnit cuResource = getCompilationUnit(resourceDomain);
		TypeDeclaration tdResource = getTypeDeclaration(cuResource);
		
		// recupera os métodos em um Map<String,List<MethodDeclaration>>
		Map<String, List<MethodDeclaration>> mapMethods = findMethodsGroupedByNameNoPrivate(type);
		
		// verifica todos os metodos, para ir adicionando as chamadas, dentro da função
		// levando em consideração parâmetros
		
		for(Entry<String, List<MethodDeclaration>> entry : mapMethods.entrySet()){
			List<MethodDeclaration> methodsSameName = entry.getValue();
			
			// são dois tipos de chamadas de método
			// existem os métodos 'normais'
			// existem os métodos 'static'
			// a 'chamada' é muito parecida, muda somente se você vai usar um objeto objeto.nomeMetodo()
			// para realizar a chamada, ou se você vai fazer a chama 'direto' Classe.nomeMetodo()
			// é necessário verificar se terá um tipo de retorno ou não
			// se tiver, dá um return, senão, não precisa
			
			//	@javax.ws.rs.Path("toString")
			//	@javax.ws.rs.POST
			//	@javax.ws.rs.Produces({javax.ws.rs.core.MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
			//	@javax.ws.rs.Consumes({javax.ws.rs.core.MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
			//	public String restCallToString(Map<String, Object> data) throws Exception{
			//		if(Utils.checkParameters(data, "restObjectBase", "properties", "name")){
			//		   	// se for static
			//			return Car.toString("properties", "name")
			//			// se não for static
			//			Car car = newCar((Map<String, Object>)data.get("restObjectBase"));
			//			return car.toString("properties", "name");
			//	    }
			//				
			//		if(Utils.checkParameters(data, "restObjectBase", "properties")){
			//	        // se for static
			//			return Car.toString("properties")
			//			// se não for static
			//			Car car = newCar((Map<String, Object>)data.get("restObjectBase"));
			//			return car.toString("properties");
			//	    }
			//	    
			//		throw new Exception("Ocorreu um erro na requisição. Número e nome dos parâmetros é inválido");
			//	}
			
			// adiciona o método newDomain
			MethodDeclaration newMethodRestCall = tdResource.getAST().newMethodDeclaration();
			// nome do método
			newMethodRestCall.setName(tdResource.getAST().newSimpleName("restCall"+methodsSameName.get(0).getName().toString()));
			
			// atualiza o typeParameter da função
			for(TypeParameter tp : (List<TypeParameter>) methodsSameName.get(0).typeParameters()){
				newMethodRestCall.typeParameters().add((TypeParameter) ASTNode.copySubtree(tdResource.getAST(), tp));
			}
			
			// flag do método
			newMethodRestCall.modifiers().addAll(tdResource.getAST().newModifiers(Modifier.PUBLIC));
			// tipo de retorno do método - Object
			newMethodRestCall.setReturnType2(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Object")));
			// parâmetro do método
			// primeiro a declaração da variável
			SingleVariableDeclaration mapParameter = tdResource.getAST().newSingleVariableDeclaration();
			mapParameter.setName(tdResource.getAST().newSimpleName("data"));
			// tipo parametrizado Map<String,Object>
			ParameterizedType mapPT = tdResource.getAST().newParameterizedType(tdResource.getAST().newSimpleType(tdResource.getAST().newName("java.util.Map")));
			mapPT.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("String")));
			mapPT.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Object")));
			mapParameter.setType(mapPT);
			newMethodRestCall.parameters().add(mapParameter);
			// throw declaration na declaração
			Type throwException = tdResource.getAST().newSimpleType(tdResource.getAST().newSimpleName("Exception"));
			newMethodRestCall.thrownExceptionTypes().add(throwException);
			Type throwThrowable = tdResource.getAST().newSimpleType(tdResource.getAST().newSimpleName("Throwable"));
			newMethodRestCall.thrownExceptionTypes().add(throwThrowable);
			// corpo
			Block bodyNewMethodRestCall = tdResource.getAST().newBlock(); 
			// seta o corpo na função
			newMethodRestCall.setBody(bodyNewMethodRestCall);
			// adiciona a função na classe
			tdResource.bodyDeclarations().add(0, newMethodRestCall);
			
			// throw dentro do corpo
			ThrowStatement throwNewException = tdResource.getAST().newThrowStatement();
			ClassInstanceCreation cicThrowException = tdResource.getAST().newClassInstanceCreation();
			StringLiteral argumentException = tdResource.getAST().newStringLiteral();
			argumentException.setLiteralValue("Ocorreu um erro na requisição. Número e nome dos parâmetros é inválido");
			cicThrowException.arguments().add(argumentException);
			cicThrowException.setType(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Exception")));
			throwNewException.setExpression(cicThrowException);
			// adiciona o throw no corpo
			bodyNewMethodRestCall.statements().add(0, throwNewException);
			
			// vai adicionando os if's para os métodos
			for(MethodDeclaration method : methodsSameName){
				// inicia o if
				IfStatement ifMethod = tdResource.getAST().newIfStatement();
				
				// chamada para Utils.checkParameters(data, "param1", "param2", ...)
				MethodInvocation utilsCheckParametersInvocation = tdResource.getAST().newMethodInvocation();
				utilsCheckParametersInvocation.setName(tdResource.getAST().newSimpleName("checkParameters"));
				utilsCheckParametersInvocation.setExpression(tdResource.getAST().newName("main.java.utils.Utils"));
				// checkParameters(data..)
				SimpleName dataArgument = tdResource.getAST().newSimpleName("data");
				utilsCheckParametersInvocation.arguments().add(dataArgument);
				// checkParameters(data, "restObjectBase")
				StringLiteral restObjectBaseArgument = tdResource.getAST().newStringLiteral();
				restObjectBaseArgument.setLiteralValue("restObjectBase");
				utilsCheckParametersInvocation.arguments().add(restObjectBaseArgument);
				// comparação dentro do if
				ifMethod.setExpression(utilsCheckParametersInvocation);
				
				MethodInvocation invocationMethod = tdResource.getAST().newMethodInvocation();
				
				Block ifBlock = tdResource.getAST().newBlock();
				ifMethod.setThenStatement(ifBlock);
				
				// tem que verificar se tem retorno
				if(method.getReturnType2().toString().contentEquals(org.eclipse.jdt.core.dom.PrimitiveType.VOID.toString())){
					ifBlock.statements().add(tdResource.getAST().newExpressionStatement(invocationMethod));
				}
				else{
					// return methodInvocation
					ReturnStatement returnMethodInvocationRestObjectBase = tdResource.getAST().newReturnStatement();
					returnMethodInvocationRestObjectBase.setExpression(invocationMethod);
					ifBlock.statements().add(returnMethodInvocationRestObjectBase);
				}
				
				// // se for static
				// return Car.toString("properties", "name")
				if(Modifier.isStatic(method.getModifiers())){
					// Car.toString(...)
					// toString(...)
					invocationMethod.setName(tdResource.getAST().newSimpleName(method.getName().toString()));
					// Car.
					invocationMethod.setExpression(tdResource.getAST().newSimpleName(clazz.getElementName()));
				}
				// // se não for static
				// Car car = newCar((Map<String, Object>)data.get("restObjectBase"));
				// return car.toString("properties", "name");
				else{
					// newCar((Map<String, Object>)data.get("restObjectBase"))
					// data.get('restObjectBase')
					// restObjectBase
					StringLiteral variableStringLiteralRestObjectBase = tdResource.getAST().newStringLiteral();
					variableStringLiteralRestObjectBase.setLiteralValue("restObjectBase");
					// data.get
					MethodInvocation dataGetInvocRestObjectBase = tdResource.getAST().newMethodInvocation();
					dataGetInvocRestObjectBase.setName(tdResource.getAST().newSimpleName("get"));
					dataGetInvocRestObjectBase.setExpression(tdResource.getAST().newName("data"));
					dataGetInvocRestObjectBase.arguments().add(variableStringLiteralRestObjectBase);
					
					// (Map<String, Object>)data.get("restObjectBase")
					// tipo parametrizado Map<String,Object>
					ParameterizedType ptRestObjectBase = tdResource.getAST().newParameterizedType(tdResource.getAST().newSimpleType(tdResource.getAST().newName("java.util.Map")));
					ptRestObjectBase.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("String")));
					ptRestObjectBase.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Object")));
					// (Map<String, Object>)data.get("restObjectBase")
					CastExpression castArgument = tdResource.getAST().newCastExpression();
					castArgument.setType(ptRestObjectBase);
					castArgument.setExpression(dataGetInvocRestObjectBase);
					
					// newCar(..)
					MethodInvocation newDomainMethodInvocationRestObjectBase = tdResource.getAST().newMethodInvocation();
					newDomainMethodInvocationRestObjectBase.setName(tdResource.getAST().newSimpleName("new"+clazz.getElementName()));
					// newCar((Map<String, Object>)data.get("restObjectBase"))
					newDomainMethodInvocationRestObjectBase.arguments().add(castArgument);
					
					// Car car ...
					// car
					VariableDeclarationFragment vdfObj = tdResource.getAST().newVariableDeclarationFragment();
					vdfObj.setName(tdResource.getAST().newSimpleName("obj"));
					vdfObj.setInitializer(newDomainMethodInvocationRestObjectBase);
					// Car car = newCar((Map<String, Object>)data.get("restObjectBase"))
					VariableDeclarationStatement vdsObj = tdResource.getAST().newVariableDeclarationStatement(vdfObj);
					vdsObj.setType(tdResource.getAST().newSimpleType(tdResource.getAST().newName(clazz.getElementName())));
					
					// adicionando no corpo do if
					ifBlock.statements().add(0, vdsObj);
					
					// car.toString(...)
					// toString(...)
					invocationMethod.setName(tdResource.getAST().newSimpleName(method.getName().toString()));
					// car.
					invocationMethod.setExpression(tdResource.getAST().newSimpleName("obj"));
				}
				
				// itera pelos parâmetros para adicionar nos argumentos
				for(SingleVariableDeclaration svdMethod : (List<SingleVariableDeclaration>)method.parameters()){
					StringLiteral variableStringLiteralCheckParameter = tdResource.getAST().newStringLiteral();
					variableStringLiteralCheckParameter.setLiteralValue(svdMethod.getName().toString());
					utilsCheckParametersInvocation.arguments().add(variableStringLiteralCheckParameter);
					
					// data.get('arg')
					// arg
					StringLiteral variableStringLiteralReturn = tdResource.getAST().newStringLiteral();
					variableStringLiteralReturn.setLiteralValue(svdMethod.getName().toString());
					// data.get
					MethodInvocation dataGetInvoc = tdResource.getAST().newMethodInvocation();
					dataGetInvoc.setName(tdResource.getAST().newSimpleName("get"));
					dataGetInvoc.setExpression(tdResource.getAST().newName("data"));
					dataGetInvoc.arguments().add(variableStringLiteralReturn);
					
					// (Type)data.get('arg')
					Type returnTypeArg;
					if(svdMethod.getExtraDimensions() > 0){
						Type typeArg = (Type) ASTNode.copySubtree(tdResource.getAST(), svdMethod.getType());
						ArrayType arrayReturnTypeArg = tdResource.getAST().newArrayType(typeArg, svdMethod.getExtraDimensions());
						returnTypeArg = arrayReturnTypeArg;
					}
					else{
						returnTypeArg = (Type) ASTNode.copySubtree(tdResource.getAST(), svdMethod.getType());
					}
					
					CastExpression castArgument = tdResource.getAST().newCastExpression();
					castArgument.setType(returnTypeArg);
					castArgument.setExpression(dataGetInvoc);
					
					invocationMethod.arguments().add(castArgument);
				}
				
				bodyNewMethodRestCall.statements().add(0, ifMethod);
				
				
			}
		}
					
		// salva as alterações realizadas na classe
		saveUpdatesCompilationUnit(cuResource, resourceDomain, resourceDocument);
	}

	/**
	 * 
	 * @param type Retorna um Map contém a dupla Nome do método : Lista de métodos com o mesmo nome
	 * @return
	 */
	private Map<String, List<MethodDeclaration>> findMethodsGroupedByNameNoPrivate(TypeDeclaration type) {
		Map<String, List<MethodDeclaration>> methods = new HashMap<String, List<MethodDeclaration>>();
		
		for(MethodDeclaration method : type.getMethods()){
			// se não for construtor e não for private 
			// lembrar que as chamadas vão utilizar 'reflection', pois descobre-se qual
			// o tipo de Objeto que foi realmente instânciado
			// senão, a conversão dará problema quando for tentar converter para um
			// tipo abstract, pois abstract não pode ser instânciado
			if(!method.isConstructor())
				if(!Modifier.isPrivate(method.getModifiers())){
				// verifica se o método existe já no map
				// se não tiver, cria lista
				List<MethodDeclaration> listMethodsEqual = methods.get(method.getName().toString());
				if(listMethodsEqual == null){
					listMethodsEqual = new ArrayList<MethodDeclaration>();
				}
				
				listMethodsEqual.add(method);
				methods.put(method.getName().toString(), listMethodsEqual);
			}
		}
		
		return methods;
	}

	/**
	 * Cria a função getDomain no resource
	 * @param clazz
	 * @param type
	 * @throws BadLocationException 
	 * @throws JavaModelException 
	 * @throws MalformedTreeException 
	 */
	private void createGetDomainFunctionSimpleType(IType clazz, TypeDeclaration type) throws MalformedTreeException, JavaModelException, BadLocationException {
		IType resourceDomain = resourcesJavaClasses.get(clazz);
		
		Document resourceDocument = getDocumentCompilationUnit(resourceDomain);
		
		CompilationUnit cuResource = getCompilationUnit(resourceDomain);
		TypeDeclaration tdResource = getTypeDeclaration(cuResource);
		
		// adiciona o método getDomain
		MethodDeclaration getDomainMethod = tdResource.getAST().newMethodDeclaration();
		// nome do método
		getDomainMethod.setName(tdResource.getAST().newSimpleName("get"+clazz.getElementName()));
		// flag do método
		getDomainMethod.modifiers().addAll(tdResource.getAST().newModifiers(Modifier.PRIVATE));
		// tipo de retorno do método
		getDomainMethod.setReturnType2(tdResource.getAST().newSimpleType(tdResource.getAST().newName(clazz.getElementName())));
		// parâmetro do método
		// primeiro a declaração da variável
		SingleVariableDeclaration mapParameter = tdResource.getAST().newSingleVariableDeclaration();
		mapParameter.setName(tdResource.getAST().newSimpleName("data"));
		// tipo parametrizado Map<String,Object>
		ParameterizedType mapPT = tdResource.getAST().newParameterizedType(tdResource.getAST().newSimpleType(tdResource.getAST().newName("java.util.Map")));
		mapPT.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("String")));
		mapPT.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Object")));
		mapParameter.setType(mapPT);
		getDomainMethod.parameters().add(mapParameter);
		// throw declaration na declaração
		Type throwException = tdResource.getAST().newSimpleType(tdResource.getAST().newSimpleName("Exception"));
		getDomainMethod.thrownExceptionTypes().add(throwException);
		Type throwThrowable = tdResource.getAST().newSimpleType(tdResource.getAST().newSimpleName("Throwable"));
		getDomainMethod.thrownExceptionTypes().add(throwThrowable);
		// corpo
		Block bodyGetDomainMethod = tdResource.getAST().newBlock(); 
		// seta o corpo na função
		getDomainMethod.setBody(bodyGetDomainMethod);
		// adiciona a função na classe
		tdResource.bodyDeclarations().add(0, getDomainMethod);
		
		// adiciona return newDomain(data)
		ReturnStatement returnNewDomain = tdResource.getAST().newReturnStatement();
		// newDomain(data)
		// newDomain(..)
		MethodInvocation newDomainMethodCall = tdResource.getAST().newMethodInvocation();
		newDomainMethodCall.setName(tdResource.getAST().newSimpleName("new"+clazz.getElementName()));
		// newDomain(data)
		SimpleName dataArgument = tdResource.getAST().newSimpleName("data");
		newDomainMethodCall.arguments().add(dataArgument);
		// return newDomain(data)
		returnNewDomain.setExpression(newDomainMethodCall);
		
		bodyGetDomainMethod.statements().add(returnNewDomain);
		
		// adiciona os annotation
		// @javax.ws.rs.Path("get")
		SingleMemberAnnotation annotationPath = tdResource.getAST().newSingleMemberAnnotation();
		annotationPath.setTypeName(tdResource.getAST().newName("javax.ws.rs.Path"));
		StringLiteral getSLPath = tdResource.getAST().newStringLiteral();
		getSLPath.setLiteralValue("get");
		annotationPath.setValue(getSLPath);
		getDomainMethod.modifiers().add(0, annotationPath); 
	    // @javax.ws.rs.POST
		MarkerAnnotation annotationPost = tdResource.getAST().newMarkerAnnotation();
		annotationPost.setTypeName(tdResource.getAST().newName("javax.ws.rs.POST"));
		getDomainMethod.modifiers().add(0, annotationPost);
	    // @javax.ws.rs.Produces({javax.ws.rs.core.MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
		SingleMemberAnnotation annotationProduces = tdResource.getAST().newSingleMemberAnnotation();
		annotationProduces.setTypeName(tdResource.getAST().newName("javax.ws.rs.Produces"));
		Name nameAppJSONProduces = tdResource.getAST().newName("javax.ws.rs.core.MediaType.APPLICATION_JSON");
		annotationProduces.setValue(nameAppJSONProduces);
		getDomainMethod.modifiers().add(0, annotationProduces);
	    // @javax.ws.rs.Consumes({javax.ws.rs.core.MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
		SingleMemberAnnotation annotationConsumes = tdResource.getAST().newSingleMemberAnnotation();
		annotationConsumes.setTypeName(tdResource.getAST().newName("javax.ws.rs.Consumes"));
		Name nameAppJSONConsumes = tdResource.getAST().newName("javax.ws.rs.core.MediaType.APPLICATION_JSON");
		annotationConsumes.setValue(nameAppJSONConsumes);
		getDomainMethod.modifiers().add(0, annotationConsumes);
		
		// salva as alterações realizadas na classe
		saveUpdatesCompilationUnit(cuResource, resourceDomain, resourceDocument);
	}

	/**
	 * Atualiza os imports da classe resource relativa ao domain
	 * @param clazz
	 * @param type
	 * @throws JavaModelException 
	 * @throws BadLocationException 
	 * @throws MalformedTreeException 
	 */
	private void updateResourceImports(IType clazz, TypeDeclaration type) throws JavaModelException, MalformedTreeException, BadLocationException {
		IType resourceDomain = resourcesJavaClasses.get(clazz);
		
		Document resourceDocument = getDocumentCompilationUnit(resourceDomain);
		
		CompilationUnit cuResource = getCompilationUnit(resourceDomain);
		
		// recupera imports da classe
		List<ImportDeclaration> importsClazz = getCompilationUnit(clazz).imports();
		// itera e verifica quais imports são necessários
		for(ImportDeclaration importClazz : importsClazz){
			if(!cuResource.toString().contains(importClazz.getName().toString())){
				cuResource.imports().add((ImportDeclaration) ASTNode.copySubtree(cuResource.getAST(), importClazz));
			}
		}
		
		// salva as alterações realizadas na classe
		saveUpdatesCompilationUnit(cuResource, resourceDomain, resourceDocument);
	}

	/** 
	 * Atualiza o resource com as funções específicas
	 * @param clazz
	 * @throws BadLocationException 
	 * @throws JavaModelException 
	 * @throws MalformedTreeException 
	 */
	private void createNewDomainFunctionSimpleType(IType clazz, TypeDeclaration typeClazz) throws MalformedTreeException, JavaModelException, BadLocationException {
		IType resourceDomain = resourcesJavaClasses.get(clazz);
		
		Document resourceDocument = getDocumentCompilationUnit(resourceDomain);
		
		CompilationUnit cuResource = getCompilationUnit(resourceDomain);
		TypeDeclaration tdResource = getTypeDeclaration(cuResource);
		
		// atualiza typeParameters (teste)
		for(TypeParameter tp : (List<TypeParameter>) typeClazz.typeParameters()){
			tdResource.typeParameters().add((TypeParameter) ASTNode.copySubtree(tdResource.getAST(), tp));
		}
		
		// adiciona o método newDomain
		MethodDeclaration newDomainMethod = tdResource.getAST().newMethodDeclaration();
		// nome do método
		newDomainMethod.setName(tdResource.getAST().newSimpleName("new"+clazz.getElementName()));
		// flag do método
		newDomainMethod.modifiers().addAll(tdResource.getAST().newModifiers(Modifier.PRIVATE));
		// tipo de retorno do método
		newDomainMethod.setReturnType2(tdResource.getAST().newSimpleType(tdResource.getAST().newName(clazz.getElementName())));
		// parâmetro do método
		// primeiro a declaração da variável
		SingleVariableDeclaration mapParameter = tdResource.getAST().newSingleVariableDeclaration();
		mapParameter.setName(tdResource.getAST().newSimpleName("data"));
		// tipo parametrizado Map<String,Object>
		ParameterizedType mapPT = tdResource.getAST().newParameterizedType(tdResource.getAST().newSimpleType(tdResource.getAST().newName("java.util.Map")));
		mapPT.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("String")));
		mapPT.typeArguments().add(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Object")));
		mapParameter.setType(mapPT);
		newDomainMethod.parameters().add(mapParameter);
		// throw declaration na declaração
		Type throwException = tdResource.getAST().newSimpleType(tdResource.getAST().newSimpleName("Exception"));
		newDomainMethod.thrownExceptionTypes().add(throwException);
		Type throwThrowable = tdResource.getAST().newSimpleType(tdResource.getAST().newSimpleName("Throwable"));
		newDomainMethod.thrownExceptionTypes().add(throwThrowable);
		// corpo
		Block bodyNewDomainMethod = tdResource.getAST().newBlock(); 
		// seta o corpo na função
		newDomainMethod.setBody(bodyNewDomainMethod);
		// adiciona a função na classe
		tdResource.bodyDeclarations().add(0, newDomainMethod);
		
		// agora, verifica todos os construtores, para ir adicionando as criações, dentro da função
		// levando em consideração parâmetros
		// se não tiver construtor nenhum, adiciona simplesmente um retorno 
		List<MethodDeclaration> constructors = findConstructorMethods(typeClazz);
		if(constructors.isEmpty()){
			// não tem nenhum construtor, então cria somente um retorno
			ReturnStatement returnZeroConstrutor = tdResource.getAST().newReturnStatement();
			// criacao do new Domain
			ClassInstanceCreation cicNewDomain = tdResource.getAST().newClassInstanceCreation();
			cicNewDomain.setType(tdResource.getAST().newSimpleType(tdResource.getAST().newName(clazz.getElementName())));
			// adiciona o new Domain no retorno
			returnZeroConstrutor.setExpression(cicNewDomain);
			// adiciona ao corpo do método
			bodyNewDomainMethod.statements().add(0, returnZeroConstrutor);
		}
		else{
			// throw dentro do corpo
			ThrowStatement throwNewException = tdResource.getAST().newThrowStatement();
			ClassInstanceCreation cicThrowException = tdResource.getAST().newClassInstanceCreation();
			StringLiteral argumentException = tdResource.getAST().newStringLiteral();
			argumentException.setLiteralValue("Ocorreu um erro na requisição. Número e nome dos parâmetros é inválido");
			cicThrowException.arguments().add(argumentException);
			cicThrowException.setType(tdResource.getAST().newSimpleType(tdResource.getAST().newName("Exception")));
			throwNewException.setExpression(cicThrowException);
			// adiciona o throw no corpo
			bodyNewDomainMethod.statements().add(0, throwNewException);
			
			// vai adicionando os if's para os construtores
			for(MethodDeclaration constructor : constructors){
				if(!Modifier.isPrivate(constructor.getModifiers())){
					// inicia o if
					IfStatement ifConstructor = tdResource.getAST().newIfStatement();
					
					// chamada para Utils.checkParameters(data, "param1", "param2", ...)
					MethodInvocation utilsCheckParametersInvocation = tdResource.getAST().newMethodInvocation();
					utilsCheckParametersInvocation.setName(tdResource.getAST().newSimpleName("checkParameters"));
					utilsCheckParametersInvocation.setExpression(tdResource.getAST().newName("main.java.utils.Utils"));
					// checkParameters(data..)
					SimpleName dataArgument = tdResource.getAST().newSimpleName("data");
					utilsCheckParametersInvocation.arguments().add(dataArgument);
					// comparação dentro do if
					ifConstructor.setExpression(utilsCheckParametersInvocation);
					// chamada para return new Domain(parameters)
					ClassInstanceCreation cicNewDomain = tdResource.getAST().newClassInstanceCreation();
					cicNewDomain.setType(tdResource.getAST().newSimpleType(tdResource.getAST().newName(clazz.getElementName())));
					ReturnStatement returnNewDomain = tdResource.getAST().newReturnStatement();
					returnNewDomain.setExpression(cicNewDomain);
					// seta o corpo do then
					ifConstructor.setThenStatement(returnNewDomain);
					
					// itera pelos parâmetros para adicionar nos argumentos
					for(SingleVariableDeclaration svdConstructor : (List<SingleVariableDeclaration>)constructor.parameters()){
						StringLiteral variableStringLiteralCheckParameter = tdResource.getAST().newStringLiteral();
						variableStringLiteralCheckParameter.setLiteralValue(svdConstructor.getName().toString());
						utilsCheckParametersInvocation.arguments().add(variableStringLiteralCheckParameter);
						
						// data.get('arg')
						// arg
						StringLiteral variableStringLiteralReturn = tdResource.getAST().newStringLiteral();
						variableStringLiteralReturn.setLiteralValue(svdConstructor.getName().toString());
						// data.get
						MethodInvocation dataGetInvoc = tdResource.getAST().newMethodInvocation();
						dataGetInvoc.setName(tdResource.getAST().newSimpleName("get"));
						dataGetInvoc.setExpression(tdResource.getAST().newName("data"));
						dataGetInvoc.arguments().add(variableStringLiteralReturn);
						
						// (Type)data.get('arg')
						Type returnTypeArg = (Type) ASTNode.copySubtree(tdResource.getAST(), svdConstructor.getType());
						CastExpression castArgument = tdResource.getAST().newCastExpression();
						castArgument.setType(returnTypeArg);
						castArgument.setExpression(dataGetInvoc);
						
						cicNewDomain.arguments().add(castArgument);
					}
					
					bodyNewDomainMethod.statements().add(0, ifConstructor);
				}
			}
		}
		
		
		// salva as alterações realizadas na classe
		saveUpdatesCompilationUnit(cuResource, resourceDomain, resourceDocument);
		
	}
	
}
