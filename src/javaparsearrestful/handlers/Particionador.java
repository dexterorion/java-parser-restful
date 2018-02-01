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
import org.eclipse.jdt.core.dom.CompilationUnit;
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
			IProject restfulProject = createRestfulProject(projects[choosenProject]);
			
			// abre objeto do projeto Java para poder modificar
			IJavaProject javaProject = JavaCore.create(restfulProject);
			
			// inicia as variáveis globais
			initializeGlobalVariable(javaProject);
			
			// processa as classes que não contém extendem de nenhuma classe ou não implementam nenhum interface
			processSimpleType(javaProject);
			
		}
		catch(CoreException e){
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

	private void enterJavaFiles(IPackageFragment mypackage) throws JavaModelException {

		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			// enter into all classes
			for (IType type : unit.getAllTypes()) {
				
			}
		}
	}

	/**
	 * Cria um projeto cópia, para serem realizadas as alterações, sem perder os dados do projeto original
	 * @param project
	 * @return Um novo projeto para ser utilizado como base para as modificações
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
	 */
	private void processSimpleType(IJavaProject project){
		
		
		
	}
	
	/**
	 * Atualiza a classe, verificando se existem construtores com parâmetro, para adicionar a anotação @JsonCreator
	 * @param type
	 */
	private void updateSimpleType(TypeDeclaration type){
		
	}
	
	/**
	 * Realiza a geração dos resources das classes que não extendem e nem implementam nenhuma classe ou interface 
	 * @param type
	 * @return
	 */
	private void generateResourceSimpleType(TypeDeclaration type){
		// 
	}
	
}
