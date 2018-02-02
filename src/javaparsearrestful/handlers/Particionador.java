package javaparsearrestful.handlers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
			
			System.out.println("Processando classes que não extendem classe e não implementam interface!");
			System.out.println(".");
			System.out.println("..");
			System.out.println("...");
			System.out.println("....");
			System.out.println(".....");
			System.out.println("......");
			// processa as classes que não contém extendem de nenhuma classe ou não implementam nenhum interface
			processSimpleType(javaProject);
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
	 * Inicializa as variáveis globais
	 * @param javaProject
	 * @throws CoreException 
	 */
	private void initializeGlobalVariable(IJavaProject javaProject) throws CoreException {
		javaClasses = new ArrayList<IType>();
		javaChildrenClasses = new ArrayList<IType>();
		javaClassesInterfaces = new ArrayList<IType>();
		javaChildrenClassesInterfaces = new ArrayList<IType>();
		javaInterfaces = new ArrayList<IType>();
		javaChildrenInterfaces = new ArrayList<IType>();
		
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
							javaChildrenClassesInterfaces.add(type);
						}
						// se não implementa interfaces
						else{
							javaChildrenClasses.add(type);
						}
					}
					// se não extende classe
					else{
						// se implementa interface
						if(implementsInterface){
							javaClassesInterfaces.add(type);
						}
						// se não implementa interfaces
						else{
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

		// faz a cÃ³pia
		IProjectDescription projectDescription = project.getDescription();
		projectDescription.setName(project.getName() + "RestfulProject");
		URI uri = URI.create(project.getLocationURI().toString()
				+ "RestfulProject");
		projectDescription.setLocationURI(uri);
		project.copy(projectDescription, true, null);

		// recupera o projeto para ser utilizado
		IWorkspaceRoot root = project.getWorkspace().getRoot();
		IProject newProject = root.getProject(project.getName()
				+ "RestfulProject");
		// newProject.create(null);
		newProject.open(null);

		return newProject;
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
	 * @param project
	 * @throws JavaModelException 
	 * @throws BadLocationException 
	 * @throws MalformedTreeException 
	 */
	private void processSimpleType(IJavaProject project) throws JavaModelException, MalformedTreeException, BadLocationException{
//		printGlobalVariableData();
		for(IType type : javaClasses){
			Document javaDocument = getDocumentCompilationUnit(type);
			CompilationUnit cuClazz = getCompilationUnit(type);
			TypeDeclaration tdClazz = getTypeDeclaration(cuClazz);
			
			updateSimpleType(tdClazz);
			
			saveUpdatesCompilationUnit(cuClazz, type, javaDocument);
			
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
	 * Atualiza a classe simples. Setar: @JsonCreator nos construtores, @JsonProperties nos parâmetros do construtor
	 * e @JsonIgnore nas funções
	 * @param type
	 */
	private void updateSimpleType(TypeDeclaration type){
		// Atualiza os construtores com parâmetros, adicionando as anotações necessárias
		updateConstructor(type);
		
	}
	
	/**
	 * Atualiza construtores
	 * @param type
	 */
	private void updateConstructor(TypeDeclaration type) {
		// retorna métodos construtores
		List<MethodDeclaration> constructors = findConstructorMethods(type); 
		
		for(MethodDeclaration constructor : constructors){
			// adiciona anotação @JsonIgnore e @JsonProperties caso seja construtor com parâmetros
			List<SingleVariableDeclaration> parameters = constructor.parameters();
			if(!parameters.isEmpty()){
				// adiciona @JsonIgnore
				NormalAnnotation annotation = constructor.getAST().newNormalAnnotation();
				annotation.setTypeName(constructor.getAST().newName("JsonIgnore"));
				constructor.modifiers().add(0, annotation);
				System.out.println("Printando");
			}
		}
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
	 */
	private void generateResourceSimpleType(TypeDeclaration type){
		// 
	}
	
}
