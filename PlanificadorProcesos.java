import java.util.*;

/**
 * Clase que representa un proceso del sistema operativo.
 */
class Proceso {
    private String id;
    private int tiempoLlegada;
    private int tiempoRafaga;
    private int tiempoRestante;
    private int tiempoInicio;
    private int tiempoFinalizacion;
    private int tiempoEspera;
    private int tiempoRetorno;

    public Proceso(String id, int tiempoLlegada, int tiempoRafaga) {
        this.id = id;
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoRafaga = tiempoRafaga;
        this.tiempoRestante = tiempoRafaga;
        this.tiempoInicio = -1;
        this.tiempoFinalizacion = 0;
        this.tiempoEspera = 0;
        this.tiempoRetorno = 0;
    }

    // Getters y Setters
    public String getId() { return id; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public int getTiempoRafaga() { return tiempoRafaga; }
    public int getTiempoRestante() { return tiempoRestante; }
    public int getTiempoInicio() { return tiempoInicio; }
    public int getTiempoFinalizacion() { return tiempoFinalizacion; }
    public int getTiempoEspera() { return tiempoEspera; }
    public int getTiempoRetorno() { return tiempoRetorno; }

    public void setTiempoRestante(int t) { this.tiempoRestante = t; }
    public void setTiempoInicio(int t) { this.tiempoInicio = t; }
    public void setTiempoFinalizacion(int t) { this.tiempoFinalizacion = t; }
    public void setTiempoEspera(int t) { this.tiempoEspera = t; }
    public void setTiempoRetorno(int t) { this.tiempoRetorno = t; }

    public void resetear() {
        this.tiempoRestante = this.tiempoRafaga;
        this.tiempoInicio = -1;
        this.tiempoFinalizacion = 0;
        this.tiempoEspera = 0;
        this.tiempoRetorno = 0;
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-12d | %-10d | %-10d | %-10d | %-12d | %-12d",
                id, tiempoLlegada, tiempoRafaga, tiempoInicio,
                tiempoFinalizacion, tiempoEspera, tiempoRetorno);
    }
}

/**
 * Interfaz que define el contrato para los algoritmos de planificación.
 */
interface AlgoritmoPlanificacion {
    void ejecutar(List<Proceso> procesos);
    void mostrarResultados(List<Proceso> procesos);
}

/**
 * Implementación del algoritmo FIFO (First In, First Out).
 * Los procesos se atienden en orden de llegada sin interrupciones.
 */
class AlgoritmoFIFO implements AlgoritmoPlanificacion {

    @Override
    public void ejecutar(List<Proceso> procesos) {
        // Ordenar por tiempo de llegada
        procesos.sort(Comparator.comparingInt(Proceso::getTiempoLlegada));

        int tiempoActual = 0;

        for (Proceso p : procesos) {
            // Si el procesador termina antes de que llegue el proceso, avanza el reloj
            if (tiempoActual < p.getTiempoLlegada()) {
                tiempoActual = p.getTiempoLlegada();
            }

            p.setTiempoInicio(tiempoActual);
            tiempoActual += p.getTiempoRafaga();
            p.setTiempoFinalizacion(tiempoActual);
            p.setTiempoRetorno(p.getTiempoFinalizacion() - p.getTiempoLlegada());
            p.setTiempoEspera(p.getTiempoRetorno() - p.getTiempoRafaga());
        }
    }

    @Override
    public void mostrarResultados(List<Proceso> procesos) {
        System.out.println("\n========================================================");
        System.out.println("       RESULTADOS - ALGORITMO FIFO");
        System.out.println("========================================================");
        System.out.printf("%-6s | %-12s | %-10s | %-10s | %-10s | %-12s | %-12s%n",
                "ID", "T.Llegada", "T.Ráfaga", "T.Inicio", "T.Fin", "T.Espera", "T.Retorno");
        System.out.println("------------------------------------------------------------------------");

        double totalEspera = 0, totalRetorno = 0;
        for (Proceso p : procesos) {
            System.out.println(p);
            totalEspera += p.getTiempoEspera();
            totalRetorno += p.getTiempoRetorno();
        }

        System.out.println("------------------------------------------------------------------------");
        System.out.printf("Tiempo promedio de espera  : %.2f%n", totalEspera / procesos.size());
        System.out.printf("Tiempo promedio de retorno : %.2f%n", totalRetorno / procesos.size());
        System.out.println("========================================================\n");
    }
}

/**
 * Implementación del algoritmo Round Robin (RR).
 * Cada proceso recibe un quantum de tiempo; si no termina, vuelve al final de la cola.
 */
class AlgoritmoRoundRobin implements AlgoritmoPlanificacion {
    private int quantum;

    public AlgoritmoRoundRobin(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public void ejecutar(List<Proceso> procesos) {
        // Ordenar por tiempo de llegada
        procesos.sort(Comparator.comparingInt(Proceso::getTiempoLlegada));

        Queue<Proceso> cola = new LinkedList<>();
        int tiempoActual = 0;
        int indice = 0;
        int n = procesos.size();
        int completados = 0;

        // Agregar procesos que llegan en tiempo 0
        while (indice < n && procesos.get(indice).getTiempoLlegada() <= tiempoActual) {
            cola.add(procesos.get(indice));
            indice++;
        }

        while (completados < n) {
            if (cola.isEmpty()) {
                // CPU inactiva, avanzar al siguiente proceso
                tiempoActual = procesos.get(indice).getTiempoLlegada();
                while (indice < n && procesos.get(indice).getTiempoLlegada() <= tiempoActual) {
                    cola.add(procesos.get(indice));
                    indice++;
                }
            }

            Proceso actual = cola.poll();

            if (actual.getTiempoInicio() == -1) {
                actual.setTiempoInicio(tiempoActual);
            }

            int tiempoEjecucion = Math.min(quantum, actual.getTiempoRestante());
            tiempoActual += tiempoEjecucion;
            actual.setTiempoRestante(actual.getTiempoRestante() - tiempoEjecucion);

            // Agregar procesos que llegaron durante este quantum
            while (indice < n && procesos.get(indice).getTiempoLlegada() <= tiempoActual) {
                cola.add(procesos.get(indice));
                indice++;
            }

            if (actual.getTiempoRestante() == 0) {
                actual.setTiempoFinalizacion(tiempoActual);
                actual.setTiempoRetorno(actual.getTiempoFinalizacion() - actual.getTiempoLlegada());
                actual.setTiempoEspera(actual.getTiempoRetorno() - actual.getTiempoRafaga());
                completados++;
            } else {
                cola.add(actual);
            }
        }
    }

    @Override
    public void mostrarResultados(List<Proceso> procesos) {
        System.out.println("\n========================================================");
        System.out.println("       RESULTADOS - ALGORITMO ROUND ROBIN (Q=" + quantum + ")");
        System.out.println("========================================================");
        System.out.printf("%-6s | %-12s | %-10s | %-10s | %-10s | %-12s | %-12s%n",
                "ID", "T.Llegada", "T.Ráfaga", "T.Inicio", "T.Fin", "T.Espera", "T.Retorno");
        System.out.println("------------------------------------------------------------------------");

        double totalEspera = 0, totalRetorno = 0;
        for (Proceso p : procesos) {
            System.out.println(p);
            totalEspera += p.getTiempoEspera();
            totalRetorno += p.getTiempoRetorno();
        }

        System.out.println("------------------------------------------------------------------------");
        System.out.printf("Tiempo promedio de espera  : %.2f%n", totalEspera / procesos.size());
        System.out.printf("Tiempo promedio de retorno : %.2f%n", totalRetorno / procesos.size());
        System.out.println("========================================================\n");
    }
}

/**
 * Clase principal que gestiona la simulación de planificación de procesos.
 */
public class PlanificadorProcesos {

    public static void main(String[] args) {
        // Definición del conjunto de procesos de prueba
        List<Proceso> procesosOriginales = new ArrayList<>();
        procesosOriginales.add(new Proceso("P1", 0, 8));
        procesosOriginales.add(new Proceso("P2", 1, 4));
        procesosOriginales.add(new Proceso("P3", 2, 9));
        procesosOriginales.add(new Proceso("P4", 3, 5));
        procesosOriginales.add(new Proceso("P5", 4, 3));

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   SIMULADOR DE PLANIFICACIÓN DE PROCESOS             ║");
        System.out.println("║   Algoritmos: FIFO y Round Robin                     ║");
        System.out.println("║   Universidad de Cartagena - Ing. de Software        ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        // -----------------------------------------------
        // Ejecutar FIFO
        // -----------------------------------------------
        List<Proceso> procesosFIFO = crearCopia(procesosOriginales);
        AlgoritmoPlanificacion fifo = new AlgoritmoFIFO();
        fifo.ejecutar(procesosFIFO);
        fifo.mostrarResultados(procesosFIFO);

        // -----------------------------------------------
        // Ejecutar Round Robin con quantum = 3
        // -----------------------------------------------
        List<Proceso> procesosRR = crearCopia(procesosOriginales);
        AlgoritmoPlanificacion rr = new AlgoritmoRoundRobin(3);
        rr.ejecutar(procesosRR);
        rr.mostrarResultados(procesosRR);
    }

    /**
     * Crea una copia profunda de la lista de procesos para ejecutar
     * cada algoritmo con datos limpios.
     */
    private static List<Proceso> crearCopia(List<Proceso> original) {
        List<Proceso> copia = new ArrayList<>();
        for (Proceso p : original) {
            copia.add(new Proceso(p.getId(), p.getTiempoLlegada(), p.getTiempoRafaga()));
        }
        return copia;
    }
}
