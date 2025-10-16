import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

// Clase NodoHuffman
class NodoHuffman implements Comparable<NodoHuffman> {
    Character caracter;
    int frecuencia;
    NodoHuffman izquierda;
    NodoHuffman derecha;
    
    public NodoHuffman(Character caracter, int frecuencia) {
        this.caracter = caracter;
        this.frecuencia = frecuencia;
        this.izquierda = null;
        this.derecha = null;
    }
    
    @Override
    public int compareTo(NodoHuffman otro) {
        return this.frecuencia - otro.frecuencia;
    }
}

// Clase CodificadorHuffman
class CodificadorHuffman {
    private Map<Character, String> diccionario;
    private NodoHuffman raiz;
    private String mensajeOriginal;
    private String mensajeCodificado;
    
    public CodificadorHuffman() {
        this.diccionario = new HashMap<>();
        this.raiz = null;
        this.mensajeOriginal = "";
        this.mensajeCodificado = "";
    }
    
    public Map<Character, String> construirArbol(String texto) {
        if (texto == null || texto.isEmpty()) {
            return diccionario;
        }
        
        this.mensajeOriginal = texto;
        
        // Contar frecuencias
        Map<Character, Integer> frecuencias = new HashMap<>();
        for (char c : texto.toCharArray()) {
            frecuencias.put(c, frecuencias.getOrDefault(c, 0) + 1);
        }
        
        // Crear cola de prioridad
        PriorityQueue<NodoHuffman> heap = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : frecuencias.entrySet()) {
            heap.add(new NodoHuffman(entry.getKey(), entry.getValue()));
        }
        
        // Construir árbol
        while (heap.size() > 1) {
            NodoHuffman izq = heap.poll();
            NodoHuffman der = heap.poll();
            
            NodoHuffman padre = new NodoHuffman(null, izq.frecuencia + der.frecuencia);
            padre.izquierda = izq;
            padre.derecha = der;
            
            heap.add(padre);
        }
        
        this.raiz = heap.poll();
        this.diccionario.clear();
        generarCodigos(this.raiz, "");
        
        return diccionario;
    }
    
    private void generarCodigos(NodoHuffman nodo, String codigoActual) {
        if (nodo == null) {
            return;
        }
        
        if (nodo.caracter != null) {
            diccionario.put(nodo.caracter, codigoActual.isEmpty() ? "0" : codigoActual);
            return;
        }
        
        generarCodigos(nodo.izquierda, codigoActual + "0");
        generarCodigos(nodo.derecha, codigoActual + "1");
    }
    
    public String codificar(String texto) {
        if (diccionario.isEmpty()) {
            construirArbol(texto);
        }
        
        StringBuilder resultado = new StringBuilder();
        for (char c : texto.toCharArray()) {
            resultado.append(diccionario.get(c));
        }
        
        this.mensajeCodificado = resultado.toString();
        return mensajeCodificado;
    }
    
    public String decodificar(String codigoBinario) {
        if (raiz == null) {
            return "";
        }
        
        StringBuilder resultado = new StringBuilder();
        NodoHuffman nodoActual = raiz;
        
        for (char bit : codigoBinario.toCharArray()) {
            if (bit == '0') {
                nodoActual = nodoActual.izquierda;
            } else {
                nodoActual = nodoActual.derecha;
            }
            
            if (nodoActual.caracter != null) {
                resultado.append(nodoActual.caracter);
                nodoActual = raiz;
            }
        }
        
        return resultado.toString();
    }
    
    public double calcularEntropia(String texto) {
        if (texto == null || texto.isEmpty()) {
            return 0.0;
        }
        
        Map<Character, Integer> frecuencias = new HashMap<>();
        for (char c : texto.toCharArray()) {
            frecuencias.put(c, frecuencias.getOrDefault(c, 0) + 1);
        }
        
        int longitud = texto.length();
        double entropia = 0.0;
        
        for (int freq : frecuencias.values()) {
            double probabilidad = (double) freq / longitud;
            entropia -= probabilidad * (Math.log(probabilidad) / Math.log(2));
        }
        
        return entropia;
    }
    
    public double calcularLargoMedio() {
        if (mensajeOriginal.isEmpty() || diccionario.isEmpty()) {
            return 0.0;
        }
        
        Map<Character, Integer> frecuencias = new HashMap<>();
        for (char c : mensajeOriginal.toCharArray()) {
            frecuencias.put(c, frecuencias.getOrDefault(c, 0) + 1);
        }
        
        int longitudTotal = mensajeOriginal.length();
        double largoMedio = 0.0;
        
        for (Map.Entry<Character, Integer> entry : frecuencias.entrySet()) {
            double probabilidad = (double) entry.getValue() / longitudTotal;
            int longitudCodigo = diccionario.get(entry.getKey()).length();
            largoMedio += probabilidad * longitudCodigo;
        }
        
        return largoMedio;
    }
    
    public double calcularEficiencia() {
        double entropia = calcularEntropia(mensajeOriginal);
        double largoMedio = calcularLargoMedio();
        
        if (largoMedio == 0) {
            return 0.0;
        }
        
        return (entropia / largoMedio) * 100;
    }
    
    public Map<Character, String> getDiccionario() {
        return diccionario;
    }
    
    public String getMensajeOriginal() {
        return mensajeOriginal;
    }
    
    public String getMensajeCodificado() {
        return mensajeCodificado;
    }
}

// Clase InterfazHuffman
public class InterfazHuffman extends JFrame {
    private CodificadorHuffman codificador;
    private JTextArea entradaTexto;
    private JTextArea salidaTexto;
    private JTextArea statsTexto;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    
    public InterfazHuffman() {
        codificador = new CodificadorHuffman();
        inicializarComponentes();
    }
    
    private void inicializarComponentes() {
        setTitle("Codificador Huffman");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 750);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de entrada
        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Mensaje de entrada"));
        entradaTexto = new JTextArea(5, 70);
        entradaTexto.setLineWrap(true);
        entradaTexto.setWrapStyleWord(true);
        JScrollPane scrollEntrada = new JScrollPane(entradaTexto);
        panelEntrada.add(scrollEntrada, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnCodificar = new JButton("Codificar");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnGuardar = new JButton("Guardar Archivo");
        
        btnCodificar.addActionListener(e -> codificarMensaje());
        btnLimpiar.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardarArchivo());
        
        panelBotones.add(btnCodificar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnGuardar);
        
        // Panel de tabla
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Diccionario (Símbolo → Código Huffman)"));
        
        String[] columnas = {"Símbolo", "ASCII/Carácter", "Código Huffman"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabla = new JTable(modeloTabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(300);
        
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setPreferredSize(new Dimension(700, 200));
        panelTabla.add(scrollTabla, BorderLayout.CENTER);
        
        // Panel de salida
        JPanel panelSalida = new JPanel(new BorderLayout());
        panelSalida.setBorder(BorderFactory.createTitledBorder("Mensaje codificado"));
        salidaTexto = new JTextArea(5, 70);
        salidaTexto.setLineWrap(true);
        salidaTexto.setWrapStyleWord(true);
        salidaTexto.setEditable(false);
        JScrollPane scrollSalida = new JScrollPane(salidaTexto);
        panelSalida.add(scrollSalida, BorderLayout.CENTER);
        
        // Panel de estadísticas
        JPanel panelStats = new JPanel(new BorderLayout());
        panelStats.setBorder(BorderFactory.createTitledBorder("Estadísticas"));
        statsTexto = new JTextArea(6, 70);
        statsTexto.setEditable(false);
        JScrollPane scrollStats = new JScrollPane(statsTexto);
        panelStats.add(scrollStats, BorderLayout.CENTER);
        
        // Agregar todos los paneles
        panelPrincipal.add(panelEntrada);
        panelPrincipal.add(Box.createVerticalStrut(10));
        panelPrincipal.add(panelBotones);
        panelPrincipal.add(Box.createVerticalStrut(10));
        panelPrincipal.add(panelTabla);
        panelPrincipal.add(Box.createVerticalStrut(10));
        panelPrincipal.add(panelSalida);
        panelPrincipal.add(Box.createVerticalStrut(10));
        panelPrincipal.add(panelStats);
        
        JScrollPane scrollPrincipal = new JScrollPane(panelPrincipal);
        add(scrollPrincipal);
    }
    
    private void codificarMensaje() {
        String mensaje = entradaTexto.getText().trim();
        
        if (mensaje.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor ingrese un mensaje para codificar",
                "Advertencia", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Limpiar tabla
        modeloTabla.setRowCount(0);
        
        // Construir árbol y codificar
        codificador.construirArbol(mensaje);
        String mensajeCodificado = codificador.codificar(mensaje);
        
        // Mostrar diccionario en tabla
        Map<Character, String> diccionario = codificador.getDiccionario();
        TreeMap<Character, String> diccionarioOrdenado = new TreeMap<>(diccionario);
        
        for (Map.Entry<Character, String> entry : diccionarioOrdenado.entrySet()) {
            char caracter = entry.getKey();
            String codigo = entry.getValue();
            
            String display;
            if (caracter == ' ') {
                display = "ESPACIO";
            } else if (caracter == '\n') {
                display = "NUEVA LÍNEA";
            } else if (caracter == '\t') {
                display = "TAB";
            } else {
                display = String.valueOf(caracter);
            }
            
            int asciiCode = (int) caracter;
            modeloTabla.addRow(new Object[]{
                display, 
                asciiCode + " (" + caracter + ")", 
                codigo
            });
        }
        
        // Mostrar mensaje codificado
        salidaTexto.setText(mensajeCodificado);
        
        // Calcular y mostrar estadísticas
        mostrarEstadisticas(mensaje, mensajeCodificado);
    }
    
    private void mostrarEstadisticas(String mensajeOriginal, String mensajeCodificado) {
        double entropia = codificador.calcularEntropia(mensajeOriginal);
        double largoMedio = codificador.calcularLargoMedio();
        double eficiencia = codificador.calcularEficiencia();
        
        int bitsOriginales = mensajeOriginal.length() * 8;
        int bitsCodificados = mensajeCodificado.length();
        double compresion = bitsOriginales > 0 ? 
            (1.0 - (double) bitsCodificados / bitsOriginales) * 100 : 0.0;
        
        String stats = String.format(
            "Entropía: %.4f bits/símbolo\n" +
            "Largo medio del código: %.4f bits/símbolo\n" +
            "Eficiencia: %.2f%%\n\n" +
            "Tamaño original: %d bits (%d caracteres × 8 bits)\n" +
            "Tamaño codificado: %d bits\n" +
            "Tasa de compresión: %.2f%%",
            entropia, largoMedio, eficiencia,
            bitsOriginales, mensajeOriginal.length(),
            bitsCodificados, compresion
        );
        
        statsTexto.setText(stats);
    }
    
    private void guardarArchivo() {
        String mensaje = entradaTexto.getText().trim();
        
        if (mensaje.isEmpty() || codificador.getMensajeCodificado().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Primero debe codificar un mensaje",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo");
        fileChooser.setSelectedFile(new File("huffman_output.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(archivo));
                
                // Escribir mensaje original
                writer.println("=== CODIFICADOR HUFFMAN ===");
                writer.println();
                writer.println("MENSAJE ORIGINAL:");
                writer.println(mensaje);
                writer.println();
                
                // Escribir diccionario
                writer.println("DICCIONARIO:");
                Map<Character, String> diccionario = codificador.getDiccionario();
                TreeMap<Character, String> diccionarioOrdenado = new TreeMap<>(diccionario);
                
                for (Map.Entry<Character, String> entry : diccionarioOrdenado.entrySet()) {
                    char c = entry.getKey();
                    String display = (c == ' ') ? "ESPACIO" : 
                                   (c == '\n') ? "NUEVA_LINEA" : 
                                   (c == '\t') ? "TAB" : String.valueOf(c);
                    writer.println(display + " (ASCII " + (int)c + ") -> " + entry.getValue());
                }
                writer.println();
                
                // Escribir mensaje codificado
                writer.println("MENSAJE CODIFICADO:");
                writer.println(codificador.getMensajeCodificado());
                writer.println();
                
                // Escribir estadísticas
                writer.println("ESTADÍSTICAS:");
                writer.printf("Entropía: %.4f bits/símbolo\n", codificador.calcularEntropia(mensaje));
                writer.printf("Largo medio: %.4f bits/símbolo\n", codificador.calcularLargoMedio());
                writer.printf("Eficiencia: %.2f%%\n", codificador.calcularEficiencia());
                
                int bitsOriginales = mensaje.length() * 8;
                int bitsCodificados = codificador.getMensajeCodificado().length();
                double compresion = (1.0 - (double) bitsCodificados / bitsOriginales) * 100;
                
                writer.printf("Bits originales: %d\n", bitsOriginales);
                writer.printf("Bits codificados: %d\n", bitsCodificados);
                writer.printf("Tasa de compresión: %.2f%%\n", compresion);
                
                writer.close();
                
                JOptionPane.showMessageDialog(this,
                    "Archivo guardado exitosamente en:\n" + archivo.getAbsolutePath(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al guardar el archivo: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void limpiar() {
        entradaTexto.setText("");
        salidaTexto.setText("");
        statsTexto.setText("");
        modeloTabla.setRowCount(0);
        codificador = new CodificadorHuffman();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            InterfazHuffman ventana = new InterfazHuffman();
            ventana.setVisible(true);
        });
    }
}