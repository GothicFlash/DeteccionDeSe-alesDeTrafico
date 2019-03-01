/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vista;

import Controlador.Conexion;
import Modelo.Señal;
import java.awt.Component;
import java.sql.SQLException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import Modelo.Usuario;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import static java.awt.image.ImageObserver.ERROR;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

/**
 *
 * @author usuario
 */
public class FrmUsuario extends javax.swing.JFrame {
    private Conexion conect;
    private String nickname;
    
    //-------------------------------------------
    String names[][] = {{"1.png","2.png"},{"Giro 1","Giro 2"}};
    private DaemonThread myThread = null;//Hilo de la camara
    VideoCapture camera = null;//Camara del dispositivo
    MatOfByte memory = new MatOfByte();//Matriz auxiliar
    MatOfRect detections = new MatOfRect();
    CascadeClassifier detector = new CascadeClassifier(getClass().getResource("trafficSignal.xml").getPath().substring(1));
    
    Mat frame = new Mat();//Matriz que contiene  el frame capturado por la camara
    Mat original;
    Mat tmp, image_roi;
    //--------------------------------------------
    /**
     * Creates new form FrmInicioSesion
     */
    class DaemonThread implements Runnable {
        protected volatile boolean runnable = false;
        @Override
        public void run(){
            synchronized(this){
                //Imgproc.resize(original, original, new Size(32,32));
                //original = new Mat(original,new Rect(8,8,16,16));
                conect = new Conexion(); //Se establece la conexión con la BD
                ArrayList<Señal> señales = conect.obtenerSeñalesActivas(); //Se obtienen las señales activas
                cerrarConexion(); //Se cierra la conexion con la BD
                while(runnable){
                    if(camera.grab()){
                        try{
                            //Imgproc.resize(original, original, new Size(32,32));
                            tmp = null;
                            image_roi = null;
                            camera.retrieve(frame);
                            
                            Graphics g = jpCamaraPrincipal.getGraphics();
                            Graphics g2 = jpCamaraSecundaria.getGraphics();
                            
                            detector.detectMultiScale(frame, detections);
                            for(Rect rect:detections.toArray()){//Itera sobre la cantidad de detecciones realizadas
                                Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x+rect.width, rect.y+rect.height), new Scalar(0,255,0));
                                image_roi = new Mat(frame, new Rect(rect.x,rect.y,rect.width+1,rect.height+1));//Guarda la ultima detección
                            }
                            
                            //Dibujado de la parte detectada en un panel diferente
                            if(image_roi!=null){
                                Imgcodecs.imencode(".bmp",image_roi, memory);
                                Image img2 = ImageIO.read(new ByteArrayInputStream(memory.toArray()));
                                BufferedImage buffer = (BufferedImage)img2;
                                g2.drawImage(buffer,0,0,jpCamaraSecundaria.getWidth(), jpCamaraSecundaria.getHeight(),0,0,buffer.getWidth(),buffer.getHeight(),null);
                                
                                //Proceso para la comparación
                                Imgproc.resize(image_roi, image_roi, new Size(32,32)); //Redimensionar imagen
                                image_roi = new Mat(image_roi,new Rect(8,8,16,16)); //recortar el area de interes
                                
                                //Encontrar la correlacion de ambas imagenes
                                double max=0;
                                int indice=-1;
                                Mat corr = new Mat();
                                for (int i = 0; i < señales.size(); i++) {
                                    //original = Imgcodecs.imread("C:\\Users\\Eliacim\\Pictures\\"+names[0][i]);//Temporal
                                    original = Imgcodecs.imread(señales.get(i).getUrlImage());
                                    Imgproc.resize(original, original, new Size(32,32));
                                    original = new Mat(original,new Rect(8,8,16,16));
                                    Imgproc.matchTemplate(image_roi, original, corr, Imgproc.TM_CCORR_NORMED);
                                    if(corr.get(0,0)[0]>max){
                                        max = corr.get(0, 0)[0];
                                        indice = i;
                                    }
                                    //System.out.println("Correlación: "+corr.get(0, 0)[0]);
                                }
                                if(indice>=0 && max>0.86){
                                    //System.out.println("Señal: "+names[1][indice]+"\tCorrelación: "+max);
                                    try {
                                        //Ponemos a "Dormir" el programa durante los ms que queremos
                                        mostrarDatos(max,señales.get(indice));
                                        registrarBitacora(señales.get(indice).getId());
                                        Thread.sleep(5000);
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                }
                                limpiarDatos();
                            }else{
                                g2.clearRect(0, 0, getWidth(), getHeight());
                            }
                            //Permite la impresion de la imagen en pantalla
                            Imgcodecs.imencode(".bmp", frame, memory);
                            Image img = ImageIO.read(new ByteArrayInputStream(memory.toArray()));
                            BufferedImage buffer = (BufferedImage)img;
                            //ImageIO.write(buffer, "jpg", new File("hola.jpg"));
                            if(g.drawImage(buffer,0,0,getWidth()-300, getHeight()-100,0,0, buffer.getWidth(), buffer.getHeight(),null)){//Actuaiza la imagem
                                if(runnable == false){
                                    System.out.println("Pausa");
                                    this.wait();
                                }
                            }
                        } catch (Exception e){
                            System.out.println("Error");
                        }
                    }
                }
            }
        }

        public void mostrarDatos(double max, Señal señal) {
            System.out.println("\tCorrelación: "+max);
            txtNombreSeñal.setText(señal.getNombre());
            lblImagenSeñal.setIcon(redimensionarImagen(señal.getUrlImage(),286,180));
            reproducirAudio(señal.getUrlAudio());
        }
        
        public ImageIcon redimensionarImagen(String ruta, int hTam, int vTam){
            ImageIcon icon = new ImageIcon(ruta);
            Image img = icon.getImage();
            Image nvaImg = img.getScaledInstance(hTam, vTam, java.awt.Image.SCALE_SMOOTH);
            ImageIcon nvoIcon = new ImageIcon(nvaImg);
            return nvoIcon;
        }
        
        public void reproducirAudio(String ruta){
            try {
                Clip sonido = AudioSystem.getClip();
                File a = new File(ruta);
                sonido.open(AudioSystem.getAudioInputStream(a));
                sonido.start();
                System.out.println("Reproduciendo 30s. de sonido...");
                Thread.sleep(1000); // 1000 milisegundos (10 segundos)
                sonido.close();
            }catch (Exception tipoerror) {
                System.out.println("" + tipoerror);
            }
        }
        
        public void registrarBitacora(int idSeñal){
            conect = new Conexion();
            int idUsuario = conect.buscarIdUsuario(nickname);
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
            String fecha = dateFormat.format(date);
            String hora = hourFormat.format(date);
            if(conect.registrarBitacora(idUsuario, idSeñal, fecha, hora)){
                System.out.println("Registro almacenado");
            }
            cerrarConexion();
        }
        
        public void limpiarDatos(){
            lblImagenSeñal.setIcon(new ImageIcon(""));
            txtNombreSeñal.setText("");
            
        }
    }
    public FrmUsuario(String nickname) {
        this.nickname = nickname;
        initComponents();
        setLocationRelativeTo(null);
        llenarEtiquetas();
        ocultarPaneles();
    }
    
    public void ocultarPaneles(){
        jpModificar.setVisible(false);
    }
    
    public void limpiarPaneles(String nombrePanel){
        switch(nombrePanel){
            case "jpModificar":
                txtNombreU.setText("");
                txtAPu.setText("");
                txtAMu.setText("");
                txtNicknameU.setText("");
                txtPassU.setText("");
                txtEmailU.setText("");
                txtConPassU.setText("");
                break;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    public void cerrarConexion(){ //Función para cerrar la conexión con la base de datos
        if (conect != null) {
            try {
                conect.Conexion().close();
            } catch ( SQLException e ) { 
                System.out.println( e.getMessage());
            }
        }
    }
    
    public void llenarEtiquetas(){ //Función para llenar un label con el nombre de usuario
        lblUser.setText(nickname);
    }
    
    private boolean validarPassword(String pass){ //Función para validar que la contraseña sea de al menos 8 digitos
        if(pass.length()<7)
            return false;
        return true;
    }
    
    private boolean verificarPasswords(String pass1, String pass2){ //Función para validar que 2 contraseñas sean iguales
        if(pass1.compareTo(pass2)==0)
            return true;
        return false;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnSalir = new javax.swing.JButton();
        jpDescripcion = new javax.swing.JPanel();
        lblEtiquetaUsr = new javax.swing.JLabel();
        lblIcon = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        jpPrincipal = new javax.swing.JPanel();
        jpCamaraPrincipal = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jpCamaraSecundaria = new javax.swing.JPanel();
        lblImagenSeñal = new javax.swing.JLabel();
        lblNombreSeñal = new javax.swing.JLabel();
        txtNombreSeñal = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        btnIniciar = new javax.swing.JButton();
        btnPausar = new javax.swing.JButton();
        jpModificar = new javax.swing.JPanel();
        jpDatosUsuario = new javax.swing.JPanel();
        btnGuardarCambiosU = new javax.swing.JButton();
        btnCancelarCambiosU = new javax.swing.JButton();
        lblNombreU = new javax.swing.JLabel();
        txtNombreU = new javax.swing.JTextField();
        txtAPu = new javax.swing.JTextField();
        lblAPu = new javax.swing.JLabel();
        lblAMu = new javax.swing.JLabel();
        txtAMu = new javax.swing.JTextField();
        txtNicknameU = new javax.swing.JTextField();
        lblNicknameU = new javax.swing.JLabel();
        lblPassU = new javax.swing.JLabel();
        txtPassU = new javax.swing.JPasswordField();
        lblEmailU = new javax.swing.JLabel();
        txtEmailU = new javax.swing.JTextField();
        lblConPassU = new javax.swing.JLabel();
        txtConPassU = new javax.swing.JPasswordField();
        cbxMostrarPassUM = new java.awt.Checkbox();
        lblIcono = new javax.swing.JLabel();
        lblFondo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnSalir.setFont(new java.awt.Font("Trebuchet MS", 1, 18)); // NOI18N
        btnSalir.setForeground(new java.awt.Color(0, 0, 204));
        btnSalir.setText("Cerrar sesión");
        btnSalir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });
        getContentPane().add(btnSalir, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 720, -1, -1));

        jpDescripcion.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(255, 255, 255)));
        jpDescripcion.setOpaque(false);

        lblEtiquetaUsr.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        lblEtiquetaUsr.setText("Sr.");

        lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/IconUsuario.png"))); // NOI18N
        lblIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblIconMouseClicked(evt);
            }
        });

        lblUsuario.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        lblUsuario.setText("Usuario:");

        lblUser.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N

        javax.swing.GroupLayout jpDescripcionLayout = new javax.swing.GroupLayout(jpDescripcion);
        jpDescripcion.setLayout(jpDescripcionLayout);
        jpDescripcionLayout.setHorizontalGroup(
            jpDescripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDescripcionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jpDescripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpDescripcionLayout.createSequentialGroup()
                        .addComponent(lblUsuario)
                        .addGap(0, 125, Short.MAX_VALUE))
                    .addGroup(jpDescripcionLayout.createSequentialGroup()
                        .addComponent(lblEtiquetaUsr)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jpDescripcionLayout.setVerticalGroup(
            jpDescripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDescripcionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpDescripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpDescripcionLayout.createSequentialGroup()
                        .addComponent(lblUsuario)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jpDescripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblEtiquetaUsr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(lblIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jpDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 10, 310, 100));

        javax.swing.GroupLayout jpCamaraPrincipalLayout = new javax.swing.GroupLayout(jpCamaraPrincipal);
        jpCamaraPrincipal.setLayout(jpCamaraPrincipalLayout);
        jpCamaraPrincipalLayout.setHorizontalGroup(
            jpCamaraPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 906, Short.MAX_VALUE)
        );
        jpCamaraPrincipalLayout.setVerticalGroup(
            jpCamaraPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 554, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Detalles"));
        jPanel2.setEnabled(false);

        javax.swing.GroupLayout jpCamaraSecundariaLayout = new javax.swing.GroupLayout(jpCamaraSecundaria);
        jpCamaraSecundaria.setLayout(jpCamaraSecundariaLayout);
        jpCamaraSecundariaLayout.setHorizontalGroup(
            jpCamaraSecundariaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jpCamaraSecundariaLayout.setVerticalGroup(
            jpCamaraSecundariaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 180, Short.MAX_VALUE)
        );

        lblNombreSeñal.setText("Nombre señal: ");

        txtNombreSeñal.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jpCamaraSecundaria, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lblNombreSeñal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNombreSeñal, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE))
                    .addComponent(lblImagenSeñal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jpCamaraSecundaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblImagenSeñal, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombreSeñal)
                    .addComponent(txtNombreSeñal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Acciones"));

        btnIniciar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Iniciar.png"))); // NOI18N
        btnIniciar.setText("Iniciar");
        btnIniciar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });

        btnPausar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Pausar.png"))); // NOI18N
        btnPausar.setText("Pausar");
        btnPausar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnPausar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPausarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(btnIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(btnPausar)
                .addGap(23, 23, 23))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPausar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jpPrincipalLayout = new javax.swing.GroupLayout(jpPrincipal);
        jpPrincipal.setLayout(jpPrincipalLayout);
        jpPrincipalLayout.setHorizontalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jpCamaraPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jpPrincipalLayout.setVerticalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpPrincipalLayout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jpCamaraPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jpPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 1270, 580));

        jpDatosUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de usuario"));

        btnGuardarCambiosU.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/GuardarCambios.png"))); // NOI18N
        btnGuardarCambiosU.setText("Guardar");
        btnGuardarCambiosU.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardarCambiosU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarCambiosUActionPerformed(evt);
            }
        });

        btnCancelarCambiosU.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Cancelar.png"))); // NOI18N
        btnCancelarCambiosU.setText("Cancelar");
        btnCancelarCambiosU.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelarCambiosU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarCambiosUActionPerformed(evt);
            }
        });

        lblNombreU.setText("Nombre: ");

        txtNombreU.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtNombreUKeyTyped(evt);
            }
        });

        txtAPu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtAPuKeyTyped(evt);
            }
        });

        lblAPu.setText("Apellido Paterno: ");

        lblAMu.setText("Apellido Materno:");

        txtAMu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtAMuKeyTyped(evt);
            }
        });

        lblNicknameU.setText("Nickname: ");

        lblPassU.setText("Password: ");

        txtPassU.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPassUFocusLost(evt);
            }
        });
        txtPassU.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtPassUKeyPressed(evt);
            }
        });

        lblEmailU.setText("Email: ");

        lblConPassU.setText("Confirmar password: ");

        cbxMostrarPassUM.setLabel("Mostrar contraseña");
        cbxMostrarPassUM.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxMostrarPassUMItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jpDatosUsuarioLayout = new javax.swing.GroupLayout(jpDatosUsuario);
        jpDatosUsuario.setLayout(jpDatosUsuarioLayout);
        jpDatosUsuarioLayout.setHorizontalGroup(
            jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosUsuarioLayout.createSequentialGroup()
                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpDatosUsuarioLayout.createSequentialGroup()
                        .addGap(483, 483, 483)
                        .addComponent(btnGuardarCambiosU)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelarCambiosU))
                    .addGroup(jpDatosUsuarioLayout.createSequentialGroup()
                        .addGap(334, 334, 334)
                        .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jpDatosUsuarioLayout.createSequentialGroup()
                                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblAPu)
                                    .addComponent(lblAMu)
                                    .addComponent(lblNicknameU)
                                    .addComponent(lblPassU)
                                    .addComponent(lblNombreU))
                                .addGap(35, 35, 35)
                                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbxMostrarPassUM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jpDatosUsuarioLayout.createSequentialGroup()
                                            .addGap(1, 1, 1)
                                            .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(txtNombreU, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtAPu)))
                                        .addComponent(txtAMu)
                                        .addComponent(txtNicknameU)
                                        .addComponent(txtPassU, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jpDatosUsuarioLayout.createSequentialGroup()
                                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblConPassU)
                                    .addComponent(lblEmailU))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtEmailU, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtConPassU, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(399, Short.MAX_VALUE))
        );
        jpDatosUsuarioLayout.setVerticalGroup(
            jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpDatosUsuarioLayout.createSequentialGroup()
                .addContainerGap(108, Short.MAX_VALUE)
                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombreU)
                    .addComponent(txtNombreU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAPu)
                    .addComponent(txtAPu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAMu)
                    .addComponent(txtAMu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNicknameU)
                    .addComponent(txtNicknameU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassU)
                    .addComponent(txtPassU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxMostrarPassUM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConPassU)
                    .addComponent(txtConPassU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmailU)
                    .addComponent(txtEmailU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(75, 75, 75)
                .addGroup(jpDatosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardarCambiosU)
                    .addComponent(btnCancelarCambiosU))
                .addContainerGap())
        );

        javax.swing.GroupLayout jpModificarLayout = new javax.swing.GroupLayout(jpModificar);
        jpModificar.setLayout(jpModificarLayout);
        jpModificarLayout.setHorizontalGroup(
            jpModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpModificarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jpDatosUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jpModificarLayout.setVerticalGroup(
            jpModificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpModificarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jpDatosUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        getContentPane().add(jpModificar, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 1270, 580));

        lblIcono.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/LogoApp.png"))); // NOI18N
        getContentPane().add(lblIcono, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 200, 100));

        lblFondo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Fondo.jpg"))); // NOI18N
        getContentPane().add(lblFondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1310, 769));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblIconMouseClicked
        // TODO add your handling code here:
        conect = new Conexion();
        Usuario usr = conect.buscarUsr(nickname);
        if(!usr.getNickname().isEmpty()){
            txtNombreU.setText(usr.getNombre());
            txtAPu.setText(usr.getaPaterno());
            txtAMu.setText(usr.getaMaterno());
            txtNicknameU.setText(usr.getNickname());
            txtPassU.setText(usr.getPassword());
            txtEmailU.setText(usr.getEmail());
            jpPrincipal.setVisible(false);
            jpModificar.setVisible(true);
        }else{
            JOptionPane.showMessageDialog(null,"Hubo un problema al obtener los datos","Error de conexión",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_lblIconMouseClicked

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        // TODO add your handling code here:
        
        if(JOptionPane.showConfirmDialog(null, "Realmente deseas cerrar sesión?", "Confirmar salida", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==0){
            Icon icon = new Icon() {
                @Override
                public int getIconWidth() {
                    return 50;
                }

                @Override
                public int getIconHeight() {
                    return 50;
                }

                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Image image = new ImageIcon(getClass().getResource("/Imagenes/IconSalir.png")).getImage();
                    g.drawImage(image, x, y, c);
                }
            };

            JOptionPane.showMessageDialog(null, "Hasta pronto Sr. "+nickname, "Que tenga un buen dia", JOptionPane.DEFAULT_OPTION, icon);
            FrmInicioS frmIs = new FrmInicioS();
            frmIs.setVisible(true);
            dispose();
        }
    }//GEN-LAST:event_btnSalirActionPerformed

    public ImageIcon redimensionarImagen(String ruta, int hTam, int vTam){
            ImageIcon icon = new ImageIcon(ruta);
            Image img = icon.getImage();
            Image nvaImg = img.getScaledInstance(hTam, vTam, java.awt.Image.SCALE_SMOOTH);
            ImageIcon nvoIcon = new ImageIcon(nvaImg);
            return nvoIcon;
        }
    
    private void btnGuardarCambiosUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarCambiosUActionPerformed
        // TODO add your handling code here:
        if(!txtNombreU.getText().isEmpty() && !txtAPu.getText().isEmpty() && !txtAMu.getText().isEmpty() && !txtNicknameU.getText().isEmpty() && !txtPassU.getText().isEmpty() && !txtEmailU.getText().isEmpty() && !txtConPassU.getText().isEmpty()){
            conect = new Conexion();
            if(conect.buscarUsuario(txtNicknameU.getText())){
                txtNicknameU.setText("");
            }else{
                Usuario usr = new Usuario();
                usr.setNombre(txtNombreU.getText());
                usr.setaMaterno(txtAMu.getText());
                usr.setaPaterno(txtAPu.getText());
                usr.setNickname(txtNicknameU.getText());
                usr.setPassword(txtPassU.getText());
                usr.setEmail(txtEmailU.getText());
                if(!conect.modificarUsuario(lblUser.getText(),usr)){
                    JOptionPane.showMessageDialog(null,"Hubo un problema al modificar los datos","Error al modificar",JOptionPane.ERROR_MESSAGE);
                }else{
                    nickname = txtNicknameU.getText();
                    lblUser.setText(nickname);
                    limpiarPaneles("jpModificar");
                    jpModificar.setVisible(false);
                    jpPrincipal.setVisible(true);
                }
            }
            cerrarConexion();
        }else{
            JOptionPane.showMessageDialog(null,"Debes de llenar todos los campos","Campos vacios",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarCambiosUActionPerformed

    private void btnCancelarCambiosUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarCambiosUActionPerformed
        // TODO add your handling code here:
        if(JOptionPane.showConfirmDialog(null, "Realmente deseas cancelar los cambios?", "Confirmar salida", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==0){
            jpModificar.setVisible(false);
            jpPrincipal.setVisible(true);
            limpiarPaneles("jpModificar");
        }
    }//GEN-LAST:event_btnCancelarCambiosUActionPerformed

    private void txtNombreUKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNombreUKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if(Character.isDigit(c)){
            JOptionPane.showMessageDialog(this,"No se pueden ingresar digitos en este campo","Error de tipos",JOptionPane.INFORMATION_MESSAGE);
            evt.consume();
        }
    }//GEN-LAST:event_txtNombreUKeyTyped

    private void txtAPuKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAPuKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if(Character.isDigit(c)){
            JOptionPane.showMessageDialog(this,"No se pueden ingresar digitos en este campo","Error de tipos",JOptionPane.INFORMATION_MESSAGE);
            evt.consume();
        }
    }//GEN-LAST:event_txtAPuKeyTyped

    private void txtAMuKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAMuKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if(Character.isDigit(c)){
            JOptionPane.showMessageDialog(this,"No se pueden ingresar digitos en este campo","Error de tipos",JOptionPane.INFORMATION_MESSAGE);
            evt.consume();
        }
    }//GEN-LAST:event_txtAMuKeyTyped

    private void txtPassUFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPassUFocusLost
        // TODO add your handling code here:
        if(!validarPassword(txtPassU.getText())){
            JOptionPane.showMessageDialog(null,"La contraseña debe tener al menos 8 caracteres o digitos","Contraseña insegura",JOptionPane.ERROR_MESSAGE);
            txtPassU.setText(null);
        }
    }//GEN-LAST:event_txtPassUFocusLost

    private void txtPassUKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPassUKeyPressed
        // TODO add your handling code here:
        if(!txtPassU.getText().isEmpty() && validarPassword(txtPassU.getText()))
        txtConPassU.setEnabled(true);
        else
        txtConPassU.setEnabled(false);
    }//GEN-LAST:event_txtPassUKeyPressed

    private void cbxMostrarPassUMItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxMostrarPassUMItemStateChanged
        // TODO add your handling code here:
        char i = txtPassU.getEchoChar();
        if (cbxMostrarPassUM.getState()) {  // a es una variable boolean en true
            txtPassU.setEchoChar((char)0); // este método es el que hace visible el texto del jPasswordField
        } else {
            txtPassU.setEchoChar('*'); // i es el char
        }
    }//GEN-LAST:event_cbxMostrarPassUMItemStateChanged

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarActionPerformed
        // TODO add your handling code here:
        camera = new VideoCapture(0);
        myThread = new DaemonThread();
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();
        btnIniciar.setEnabled(false);
        btnPausar.setEnabled(true);
    }//GEN-LAST:event_btnIniciarActionPerformed

    private void btnPausarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPausarActionPerformed
        // TODO add your handling code here:
        myThread.runnable = false;
        btnPausar.setEnabled(false);
        btnIniciar.setEnabled(true);
        jpCamaraPrincipal.removeAll();
        jpCamaraPrincipal.repaint();
        jpCamaraSecundaria.removeAll();
        jpCamaraSecundaria.repaint();
        lblImagenSeñal.setIcon(new ImageIcon(""));
        txtNombreSeñal.setText("");
        camera.release();
    }//GEN-LAST:event_btnPausarActionPerformed

    /**
     * @param args the command line arguments
     */
    /*
    public static void main(String args[]) {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME); //Linea necesaria para las librerias Mat
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmUsuario("").setVisible(true);
            }
        });
    }*/

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelarCambiosU;
    private javax.swing.JButton btnGuardarCambiosU;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnPausar;
    private javax.swing.JButton btnSalir;
    private java.awt.Checkbox cbxMostrarPassUM;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jpCamaraPrincipal;
    private javax.swing.JPanel jpCamaraSecundaria;
    private javax.swing.JPanel jpDatosUsuario;
    private javax.swing.JPanel jpDescripcion;
    private javax.swing.JPanel jpModificar;
    private javax.swing.JPanel jpPrincipal;
    private javax.swing.JLabel lblAMu;
    private javax.swing.JLabel lblAPu;
    private javax.swing.JLabel lblConPassU;
    private javax.swing.JLabel lblEmailU;
    private javax.swing.JLabel lblEtiquetaUsr;
    private javax.swing.JLabel lblFondo;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblIcono;
    private javax.swing.JLabel lblImagenSeñal;
    private javax.swing.JLabel lblNicknameU;
    private javax.swing.JLabel lblNombreSeñal;
    private javax.swing.JLabel lblNombreU;
    private javax.swing.JLabel lblPassU;
    private javax.swing.JLabel lblUser;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JTextField txtAMu;
    private javax.swing.JTextField txtAPu;
    private javax.swing.JPasswordField txtConPassU;
    private javax.swing.JTextField txtEmailU;
    private javax.swing.JTextField txtNicknameU;
    private javax.swing.JTextField txtNombreSeñal;
    private javax.swing.JTextField txtNombreU;
    private javax.swing.JPasswordField txtPassU;
    // End of variables declaration//GEN-END:variables
}
