package javaparsearrestful.handlers;

import java.net.URI;
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

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class Particionador extends AbstractHandler {
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
			IProject remoteProject = createRemoteProject(projects[choosenProject]);
		}
		catch(CoreException e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Cria um projeto cópia, para serem realizadas as alterações, sem perder os dados do projeto original
	 * @param project
	 * @return Um novo projeto para ser utilizado como base para as modificações
	 * @throws CoreException
	 */
	private IProject createRemoteProject(IProject project) throws CoreException {
		if (!project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
			System.out.println("Esse nÃ£o Ã© um projeto Java. Terminando.");
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
}
