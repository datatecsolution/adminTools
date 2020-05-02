package net.datatecsolution.admin_tools.config;
import java.lang.Thread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CreadorSubproceso {
	
	public static void main( String[] args )
	{
		// crea y nombra cada objeto Runnable
		TareaImprimir tarea1 = new TareaImprimir( "tarea1" );
		TareaImprimir tarea2 = new TareaImprimir( "tarea2" );
		TareaImprimir tarea3 = new TareaImprimir( "tarea3" );
		
		System.out.println( "Iniciando Executor" );
		
		// crea objeto ExecutorService para administrar los subprocesos
		ExecutorService ejecutorSubprocesos = Executors.newCachedThreadPool();
		
		// inicia los subprocesos y los coloca en el estado ejecutable
		ejecutorSubprocesos.execute( tarea1 ); // inicia tarea1
		ejecutorSubprocesos.execute( tarea2 ); // inicia tarea2
		ejecutorSubprocesos.execute( tarea3 ); // inicia tarea3
		
		// cierra los subprocesos trabajadores cuando terminan sus tareas
		ejecutorSubprocesos.shutdown();
		
		System.out.println( "Tareas iniciadas, main termina.\n" );
		
		
		/*
		 * 
		System.out.println( "Creacion de subprocesos" );
		
		 // crea cada subproceso con un nuevo objeto Runnable
		Thread subproceso1 = new Thread( new TareaImprimir( "tarea1" ) );
		Thread subproceso2 = new Thread( new TareaImprimir( "tarea2" ) );
		Thread subproceso3 = new Thread( new TareaImprimir( "tarea3" ) );
		System.out.println( "Subprocesos creados, iniciando tareas." );
		
		 // inicia los subprocesos y los coloca en el estado "en ejecución"
		subproceso1.start(); // invoca al método run de tarea1
		subproceso2.start(); // invoca al método run de tarea2
		subproceso3.start(); // invoca al método run de tarea3
		
		System.out.println( "Tareas iniciadas, main termina.\n" );*/
	}

}
