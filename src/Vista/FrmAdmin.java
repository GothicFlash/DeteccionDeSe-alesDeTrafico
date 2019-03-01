/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vista;

import Vista.*;
import Controlador.Conexion;
import Modelo.Usuario;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Calendar;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author usuario
 */
public class FrmAdmin extends javax.swing.JFrame {
    private String tipoAudio = "wav";
    private String tipoImagen = "png";
    private FileNameExtensionFilter filterImage = new FileNameExtensionFilter("Archivo de imagen",tipoImagen); //Filtro para imagenes
    private FileNameExtensionFilter filterAudio = new FileNameExtensionFilter("Archivo de audio",tipoAudio); //Filtro para audios
    private Conexion conect;
    private String nickname, rutaImagen, rutaAudio, nvaRutaAudio, nvaRutaImagen;
    private int seleccion;
    /**
     * Creates new form FrmInicioSesion
     */
    public FrmAdmin(String nickname) {
        this.nickname = nickname;
        this.rutaAudio = "";
        this.rutaImagen = "";
        initComponents();
        setLocationRelativeTo(null);
        llenarEtiquetas();
        llenarComboBox();
        ocultarPaneles();
    }
    
    public void cambiarRutas(){ //Agrega un % a cada \ para que se agregue a la BD
        if(!nvaRutaAudio.isEmpty())
            nvaRutaAudio = nvaRutaAudio.replace('\\', '%');
        if(!nvaRutaImagen.isEmpty())
            nvaRutaImagen = nvaRutaImagen.replace('\\', '%');
    }
    
    private String cambiarFormatoFecha(){ //Función para convertir un Date a un String con formato de fecha yyyy-MM-dd
        String fechamod;
        String dia = Integer.toString(jdFecha.getCalendar().get(Calendar.DAY_OF_MONTH));
        String mes = Integer.toString(jdFecha.getCalendar().get(Calendar.MONTH) + 1);
        String year = Integer.toString(jdFecha.getCalendar().get(Calendar.YEAR));
        fechamod = (year + "-" + mes+ "-" + dia);
        return fechamod;
    }
    
    public void llenarComboBox(){
        conect = new Conexion();
        conect.obtenerUsuarios(cbxUsuario);
        conect.obtenerSeñales(cbxSeñal);
        cerrarConexion();
    }

    public void ocultarPaneles(){
        jpSeñales.setVisible(false);
        jpBitacora.setVisible(false);
        jpUsuarios.setVisible(false);
        jpAgregar.setVisible(false);
        jpModificar.setVisible(false);
    }
    
    public void actualizarSeñales(){
        conect = new Conexion();
        conect.obtenerSeñales(tblSeñales);
        cerrarConexion();
    }
    
    public void habilitarBotonesPanelSeñales(String accion){
        if(accion=="Guardar"){
            actualizarSeñales();
            JOptionPane.showMessageDialog(null,"Los cambios han sido guardados","Cambios guardados",JOptionPane.PLAIN_MESSAGE);
        }else{
            if(accion=="Eliminar"){
                actualizarSeñales();
                JOptionPane.showMessageDialog(null,"La señal ha sido eliminada","Señal eliminada",JOptionPane.PLAIN_MESSAGE);
            }
        }
        btnAgregar.setEnabled(true);
        btnEliminar.setEnabled(false);
        btnModificar.setEnabled(false);
        btnGuardarCambios.setEnabled(false);
        btnCancelarCambios.setEnabled(false);
        txtNombreM.setEnabled(false);
        btnAudioM.setEnabled(false);
    }
    
    public String copiarArchivo(String rutaOrigen, String nombre, String tipo){
        String carpeta = (tipo==tipoImagen)?"\\Senales\\":"\\Audios\\";
        String nvaRuta = System.getProperty("user.dir")+carpeta+nombre+"."+tipo;
        try{
            FileInputStream fis = new FileInputStream(rutaOrigen);
            FileOutputStream fos = new FileOutputStream(nvaRuta);
            FileChannel inChannel = fis.getChannel(); 
            FileChannel outChannel = fos.getChannel(); 
            inChannel.transferTo(0, inChannel.size(), outChannel); 
            fis.close(); 
            fos.close();
        }catch (IOException ioe) {
            System.err.println("Error al Generar Copia");
        }
        return nvaRuta;
    }
    
    public void limpiarPaneles(String nombrePanel){
        switch(nombrePanel){
            case "jpSeñales":
                txtRutaA.setText("");
                txtNombreM.setText("");
                break;
            case "jpBitacora":
                jdFecha.setDate(null);
                cbxUsuario.setSelectedIndex(0);
                if(cbxSeñal.getItemCount()>0){
                    cbxSeñal.setSelectedIndex(0);
                }
                txtNombreS.setText("");
                lblVerSeñal.setIcon(new ImageIcon(""));
                break;
            case "jpUsuarios":
                txtNombreUsuario.setText("");
                txtAPusuario.setText("");
                txtAMusuario.setText("");
                txtNicknameUsuario.setText("");
                txtPassUsuario.setText("");
                txtEmailUsuario.setText("");
                cmbxTipoUsuario.setSelectedIndex(0);
                cmbxAccesoUsuario.setSelectedIndex(0);
                cbxMostrarPassUsuario.setState(false);
                break;
            case "jpAgregar":
                txtNombreImg.setText("");
                txtRutaImagen.setText("");
                txtRutaAudio.setText("");
                lblImagenS.setIcon(new ImageIcon(""));
                break;
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
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgrpBusqueda = new javax.swing.ButtonGroup();
        jpPrincipal = new javax.swing.JPanel();
        btnSeñales = new javax.swing.JButton();
        btnBitacora = new javax.swing.JButton();
        btnUsuarios = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        jpDescripcion = new javax.swing.JPanel();
        lblIcon = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        lblEtiquetaUsr = new javax.swing.JLabel();
        jpSeñales = new javax.swing.JPanel();
        btnAtras = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lblImagenS = new javax.swing.JLabel();
        lblRutaA = new javax.swing.JLabel();
        txtRutaA = new javax.swing.JTextField();
        btnAudioM = new javax.swing.JButton();
        lblNombreM = new javax.swing.JLabel();
        txtNombreM = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        btnAgregar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSeñales = new javax.swing.JTable();
        btnGuardarCambios = new javax.swing.JButton();
        btnCancelarCambios = new javax.swing.JButton();
        jpBitacora = new javax.swing.JPanel();
        btnAtras1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jpFiltros = new javax.swing.JPanel();
        rbtnFiltroTodos = new javax.swing.JRadioButton();
        rbtnFiltroFecha = new javax.swing.JRadioButton();
        rbtnFiltroUsuario = new javax.swing.JRadioButton();
        rbtnFiltroSeñal = new javax.swing.JRadioButton();
        jpFFecha = new javax.swing.JPanel();
        jdFecha = new com.toedter.calendar.JDateChooser();
        lblFecha = new javax.swing.JLabel();
        btnBuscar = new javax.swing.JButton();
        jpFUsuario = new javax.swing.JPanel();
        lblNUsuario = new javax.swing.JLabel();
        cbxUsuario = new javax.swing.JComboBox<>();
        jpFSeñal = new javax.swing.JPanel();
        lblSeñal = new javax.swing.JLabel();
        cbxSeñal = new javax.swing.JComboBox<>();
        jpRegistros = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblBitacora = new javax.swing.JTable();
        jpImagen = new javax.swing.JPanel();
        lblVerSeñal = new javax.swing.JLabel();
        lblNombreS = new javax.swing.JLabel();
        txtNombreS = new javax.swing.JTextField();
        jpAgregar = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        jpDatosSeñal = new javax.swing.JPanel();
        lblNombreImg = new javax.swing.JLabel();
        txtNombreImg = new javax.swing.JTextField();
        lblRutaImagen = new javax.swing.JLabel();
        txtRutaImagen = new javax.swing.JTextField();
        btnImagen = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        lblPrevisualizar = new javax.swing.JLabel();
        jpDatosAudio = new javax.swing.JPanel();
        btnAudio = new javax.swing.JButton();
        txtRutaAudio = new javax.swing.JTextField();
        lblRutaImagen1 = new javax.swing.JLabel();
        btnCancelar = new javax.swing.JButton();
        jpUsuarios = new javax.swing.JPanel();
        btnAtras2 = new javax.swing.JButton();
        jpVerUsuarios = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        jpDetalleUsuario = new javax.swing.JPanel();
        lblNombreUsuario = new javax.swing.JLabel();
        lblAPusuario = new javax.swing.JLabel();
        lblAMusuario = new javax.swing.JLabel();
        lblNicknameUsuario = new javax.swing.JLabel();
        lblPassUsuario = new javax.swing.JLabel();
        lblEmailUsuario = new javax.swing.JLabel();
        lblTipoUsuario = new javax.swing.JLabel();
        lblAccesoUsuario = new javax.swing.JLabel();
        txtNombreUsuario = new javax.swing.JTextField();
        txtAPusuario = new javax.swing.JTextField();
        txtAMusuario = new javax.swing.JTextField();
        txtNicknameUsuario = new javax.swing.JTextField();
        txtPassUsuario = new javax.swing.JPasswordField();
        txtEmailUsuario = new javax.swing.JTextField();
        cmbxTipoUsuario = new javax.swing.JComboBox<>();
        cmbxAccesoUsuario = new javax.swing.JComboBox<>();
        btnModificarUsuario = new javax.swing.JButton();
        btnGuardarCambiosUsuario = new javax.swing.JButton();
        btnCancelarCambiosUsuario = new javax.swing.JButton();
        cbxMostrarPassUsuario = new java.awt.Checkbox();
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

        jpPrincipal.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jpPrincipal.setLayout(null);

        btnSeñales.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Agregar.png"))); // NOI18N
        btnSeñales.setText("Mis señales");
        btnSeñales.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSeñales.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSeñales.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        btnSeñales.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSeñales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSeñalesActionPerformed(evt);
            }
        });
        jpPrincipal.add(btnSeñales);
        btnSeñales.setBounds(12, 13, 184, 225);

        btnBitacora.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Bitacora.png"))); // NOI18N
        btnBitacora.setText("Bitacora");
        btnBitacora.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBitacora.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnBitacora.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        btnBitacora.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnBitacora.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBitacoraActionPerformed(evt);
            }
        });
        jpPrincipal.add(btnBitacora);
        btnBitacora.setBounds(214, 13, 184, 225);

        btnUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Usuarios.png"))); // NOI18N
        btnUsuarios.setText("Usuarios");
        btnUsuarios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUsuarios.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUsuarios.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        btnUsuarios.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuariosActionPerformed(evt);
            }
        });
        jpPrincipal.add(btnUsuarios);
        btnUsuarios.setBounds(416, 13, 184, 225);

        getContentPane().add(jpPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 1270, 580));

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

        lblEtiquetaUsr.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        lblEtiquetaUsr.setText("Sr.");

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                            .addComponent(lblEtiquetaUsr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(lblIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jpDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 10, 310, 100));

        btnAtras.setText("<<Atras");
        btnAtras.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAtras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtrasActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Señal"));

        lblRutaA.setText("Audio: ");

        txtRutaA.setEnabled(false);

        btnAudioM.setText("...");
        btnAudioM.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAudioM.setEnabled(false);
        btnAudioM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAudioMActionPerformed(evt);
            }
        });

        lblNombreM.setText("Nombre: ");

        txtNombreM.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtNombreM.setEnabled(false);
        txtNombreM.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNombreMFocusLost(evt);
            }
        });
        txtNombreM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNombreMKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblImagenS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblRutaA)
                        .addGap(12, 12, 12)
                        .addComponent(txtRutaA, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAudioM, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblNombreM)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtNombreM)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblImagenS, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombreM)
                    .addComponent(txtNombreM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRutaA)
                    .addComponent(txtRutaA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAudioM))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Opciones"));

        btnAgregar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/AgregarS.png"))); // NOI18N
        btnAgregar.setText("Agregar");
        btnAgregar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });

        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Eliminar.png"))); // NOI18N
        btnEliminar.setText("Eliminar");
        btnEliminar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnModificar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/ModificarS.png"))); // NOI18N
        btnModificar.setText("Modificar");
        btnModificar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnModificar.setEnabled(false);
        btnModificar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnModificarMouseClicked(evt);
            }
        });

        tblSeñales.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Nombre", "Ruta", "Audio"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, true, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSeñales.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSeñalesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblSeñales);

        btnGuardarCambios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/GuardarCambios.png"))); // NOI18N
        btnGuardarCambios.setText("Guardar");
        btnGuardarCambios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardarCambios.setEnabled(false);
        btnGuardarCambios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarCambiosActionPerformed(evt);
            }
        });

        btnCancelarCambios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Cancelar.png"))); // NOI18N
        btnCancelarCambios.setText("Cancelar");
        btnCancelarCambios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelarCambios.setEnabled(false);
        btnCancelarCambios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarCambiosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnAgregar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEliminar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnModificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGuardarCambios)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelarCambios)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregar)
                    .addComponent(btnEliminar)
                    .addComponent(btnModificar)
                    .addComponent(btnGuardarCambios)
                    .addComponent(btnCancelarCambios))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 411, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jpSeñalesLayout = new javax.swing.GroupLayout(jpSeñales);
        jpSeñales.setLayout(jpSeñalesLayout);
        jpSeñalesLayout.setHorizontalGroup(
            jpSeñalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpSeñalesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpSeñalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpSeñalesLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnAtras))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpSeñalesLayout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jpSeñalesLayout.setVerticalGroup(
            jpSeñalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpSeñalesLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jpSeñalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAtras)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jpSeñales, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 1270, 580));

        btnAtras1.setText("<<Atras");
        btnAtras1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAtras1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtras1ActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Opciones"));

        jpFiltros.setBorder(javax.swing.BorderFactory.createTitledBorder("Buscar"));

        bgrpBusqueda.add(rbtnFiltroTodos);
        rbtnFiltroTodos.setSelected(true);
        rbtnFiltroTodos.setText("Todos");
        rbtnFiltroTodos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtnFiltroTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtnFiltroTodosActionPerformed(evt);
            }
        });

        bgrpBusqueda.add(rbtnFiltroFecha);
        rbtnFiltroFecha.setText("Fecha");
        rbtnFiltroFecha.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtnFiltroFecha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtnFiltroFechaActionPerformed(evt);
            }
        });

        bgrpBusqueda.add(rbtnFiltroUsuario);
        rbtnFiltroUsuario.setText("Usuario");
        rbtnFiltroUsuario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtnFiltroUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtnFiltroUsuarioActionPerformed(evt);
            }
        });

        bgrpBusqueda.add(rbtnFiltroSeñal);
        rbtnFiltroSeñal.setText("Señal");
        rbtnFiltroSeñal.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rbtnFiltroSeñal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtnFiltroSeñalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpFiltrosLayout = new javax.swing.GroupLayout(jpFiltros);
        jpFiltros.setLayout(jpFiltrosLayout);
        jpFiltrosLayout.setHorizontalGroup(
            jpFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpFiltrosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpFiltrosLayout.createSequentialGroup()
                        .addComponent(rbtnFiltroTodos)
                        .addGap(18, 18, 18)
                        .addComponent(rbtnFiltroFecha)
                        .addGap(18, 18, 18)
                        .addComponent(rbtnFiltroUsuario))
                    .addComponent(rbtnFiltroSeñal))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpFiltrosLayout.setVerticalGroup(
            jpFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpFiltrosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtnFiltroTodos)
                    .addComponent(rbtnFiltroFecha)
                    .addComponent(rbtnFiltroUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rbtnFiltroSeñal)
                .addContainerGap())
        );

        jpFFecha.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtrar por fecha"));
        jpFFecha.setEnabled(false);

        jdFecha.setEnabled(false);

        lblFecha.setText("Fecha:");
        lblFecha.setEnabled(false);

        javax.swing.GroupLayout jpFFechaLayout = new javax.swing.GroupLayout(jpFFecha);
        jpFFecha.setLayout(jpFFechaLayout);
        jpFFechaLayout.setHorizontalGroup(
            jpFFechaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpFFechaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblFecha)
                .addGap(31, 31, 31)
                .addComponent(jdFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpFFechaLayout.setVerticalGroup(
            jpFFechaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpFFechaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpFFechaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblFecha)
                    .addComponent(jdFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Buscar.png"))); // NOI18N
        btnBuscar.setText("Buscar");
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        jpFUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtrar por usuario"));
        jpFUsuario.setEnabled(false);

        lblNUsuario.setText("Usuario: ");
        lblNUsuario.setEnabled(false);

        cbxUsuario.setEnabled(false);

        javax.swing.GroupLayout jpFUsuarioLayout = new javax.swing.GroupLayout(jpFUsuario);
        jpFUsuario.setLayout(jpFUsuarioLayout);
        jpFUsuarioLayout.setHorizontalGroup(
            jpFUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpFUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblNUsuario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxUsuario, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );
        jpFUsuarioLayout.setVerticalGroup(
            jpFUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpFUsuarioLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jpFUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNUsuario)
                    .addComponent(cbxUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jpFSeñal.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtrar por señal"));
        jpFSeñal.setEnabled(false);

        lblSeñal.setText("Señal: ");
        lblSeñal.setEnabled(false);

        cbxSeñal.setEnabled(false);

        javax.swing.GroupLayout jpFSeñalLayout = new javax.swing.GroupLayout(jpFSeñal);
        jpFSeñal.setLayout(jpFSeñalLayout);
        jpFSeñalLayout.setHorizontalGroup(
            jpFSeñalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpFSeñalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSeñal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbxSeñal, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jpFSeñalLayout.setVerticalGroup(
            jpFSeñalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpFSeñalLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jpFSeñalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSeñal)
                    .addComponent(cbxSeñal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jpFFecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jpFiltros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jpFUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jpFSeñal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBuscar)
                .addGap(80, 80, 80))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jpFiltros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jpFFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpFUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jpFSeñal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(83, 83, 83)
                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );

        jpRegistros.setBorder(javax.swing.BorderFactory.createTitledBorder("Registros"));

        tblBitacora.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Nickname", "Fecha", "Hora", "Señal", "Ruta", "Estado señal"
            }
        ));
        tblBitacora.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBitacoraMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblBitacora);

        javax.swing.GroupLayout jpRegistrosLayout = new javax.swing.GroupLayout(jpRegistros);
        jpRegistros.setLayout(jpRegistrosLayout);
        jpRegistrosLayout.setHorizontalGroup(
            jpRegistrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpRegistrosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
                .addContainerGap())
        );
        jpRegistrosLayout.setVerticalGroup(
            jpRegistrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpRegistrosLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 461, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jpImagen.setBorder(javax.swing.BorderFactory.createTitledBorder("Imagen"));

        lblNombreS.setText("Nombre: ");

        txtNombreS.setEnabled(false);

        javax.swing.GroupLayout jpImagenLayout = new javax.swing.GroupLayout(jpImagen);
        jpImagen.setLayout(jpImagenLayout);
        jpImagenLayout.setHorizontalGroup(
            jpImagenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpImagenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpImagenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblVerSeñal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jpImagenLayout.createSequentialGroup()
                        .addComponent(lblNombreS)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtNombreS, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jpImagenLayout.setVerticalGroup(
            jpImagenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpImagenLayout.createSequentialGroup()
                .addComponent(lblVerSeñal, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jpImagenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombreS)
                    .addComponent(txtNombreS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jpBitacoraLayout = new javax.swing.GroupLayout(jpBitacora);
        jpBitacora.setLayout(jpBitacoraLayout);
        jpBitacoraLayout.setHorizontalGroup(
            jpBitacoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpBitacoraLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpBitacoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpBitacoraLayout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jpRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jpImagen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpBitacoraLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnAtras1)))
                .addContainerGap())
        );
        jpBitacoraLayout.setVerticalGroup(
            jpBitacoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpBitacoraLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpBitacoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpBitacoraLayout.createSequentialGroup()
                        .addGroup(jpBitacoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jpRegistros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 36, Short.MAX_VALUE))
                    .addGroup(jpBitacoraLayout.createSequentialGroup()
                        .addComponent(jpImagen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAtras1)))
                .addContainerGap())
        );

        getContentPane().add(jpBitacora, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 1270, 580));

        jpAgregar.setPreferredSize(new java.awt.Dimension(1270, 580));

        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Guardar.png"))); // NOI18N
        btnGuardar.setText("Guardar");
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        jpDatosSeñal.setBorder(javax.swing.BorderFactory.createTitledBorder("Señal"));

        lblNombreImg.setText("Nombre: ");

        txtNombreImg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNombreImgKeyReleased(evt);
            }
        });

        lblRutaImagen.setText("Imagen: ");

        txtRutaImagen.setEnabled(false);

        btnImagen.setText("...");
        btnImagen.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImagen.setEnabled(false);
        btnImagen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImagenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpDatosSeñalLayout = new javax.swing.GroupLayout(jpDatosSeñal);
        jpDatosSeñal.setLayout(jpDatosSeñalLayout);
        jpDatosSeñalLayout.setHorizontalGroup(
            jpDatosSeñalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosSeñalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpDatosSeñalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jpDatosSeñalLayout.createSequentialGroup()
                        .addComponent(lblRutaImagen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtRutaImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 536, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnImagen))
                    .addGroup(jpDatosSeñalLayout.createSequentialGroup()
                        .addComponent(lblNombreImg)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtNombreImg)))
                .addContainerGap(48, Short.MAX_VALUE))
        );
        jpDatosSeñalLayout.setVerticalGroup(
            jpDatosSeñalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosSeñalLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(jpDatosSeñalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombreImg)
                    .addComponent(txtNombreImg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                .addGroup(jpDatosSeñalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRutaImagen)
                    .addComponent(txtRutaImagen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnImagen))
                .addGap(37, 37, 37))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Previsualizar imagen"));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPrevisualizar, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPrevisualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jpDatosAudio.setBorder(javax.swing.BorderFactory.createTitledBorder("Audio"));

        btnAudio.setText("...");
        btnAudio.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAudio.setEnabled(false);
        btnAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAudioActionPerformed(evt);
            }
        });

        txtRutaAudio.setEnabled(false);

        lblRutaImagen1.setText("Audio:");

        javax.swing.GroupLayout jpDatosAudioLayout = new javax.swing.GroupLayout(jpDatosAudio);
        jpDatosAudio.setLayout(jpDatosAudioLayout);
        jpDatosAudioLayout.setHorizontalGroup(
            jpDatosAudioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosAudioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblRutaImagen1)
                .addGap(18, 18, 18)
                .addComponent(txtRutaAudio, javax.swing.GroupLayout.PREFERRED_SIZE, 535, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnAudio)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpDatosAudioLayout.setVerticalGroup(
            jpDatosAudioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosAudioLayout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addGroup(jpDatosAudioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRutaImagen1)
                    .addComponent(txtRutaAudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAudio))
                .addContainerGap(98, Short.MAX_VALUE))
        );

        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Cancelar.png"))); // NOI18N
        btnCancelar.setText("Cancelar");
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpAgregarLayout = new javax.swing.GroupLayout(jpAgregar);
        jpAgregar.setLayout(jpAgregarLayout);
        jpAgregarLayout.setHorizontalGroup(
            jpAgregarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jpAgregarLayout.createSequentialGroup()
                .addGroup(jpAgregarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jpAgregarLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnGuardar)
                        .addGap(18, 18, 18)
                        .addComponent(btnCancelar))
                    .addGroup(jpAgregarLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jpAgregarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jpDatosSeñal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jpDatosAudio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );
        jpAgregarLayout.setVerticalGroup(
            jpAgregarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpAgregarLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jpAgregarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jpAgregarLayout.createSequentialGroup()
                        .addComponent(jpDatosSeñal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jpDatosAudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(jpAgregarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnCancelar))
                .addGap(34, 34, 34))
        );

        getContentPane().add(jpAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 1270, 580));

        btnAtras2.setText("<<Atras");
        btnAtras2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAtras2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtras2ActionPerformed(evt);
            }
        });

        jpVerUsuarios.setBorder(javax.swing.BorderFactory.createTitledBorder("Lista de usuarios"));

        tblUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Nickname"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUsuariosMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblUsuarios);

        javax.swing.GroupLayout jpVerUsuariosLayout = new javax.swing.GroupLayout(jpVerUsuarios);
        jpVerUsuarios.setLayout(jpVerUsuariosLayout);
        jpVerUsuariosLayout.setHorizontalGroup(
            jpVerUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpVerUsuariosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                .addContainerGap())
        );
        jpVerUsuariosLayout.setVerticalGroup(
            jpVerUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpVerUsuariosLayout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                .addContainerGap())
        );

        jpDetalleUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder("Detalle usuario"));

        lblNombreUsuario.setText("Nombre: ");

        lblAPusuario.setText("Apellido Paterno: ");

        lblAMusuario.setText("Apellido Materno:");

        lblNicknameUsuario.setText("Nickname: ");

        lblPassUsuario.setText("Password: ");

        lblEmailUsuario.setText("Email: ");

        lblTipoUsuario.setText("Tipo: ");

        lblAccesoUsuario.setText("Acceso:");

        txtNombreUsuario.setEnabled(false);
        txtNombreUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtNombreUsuarioKeyTyped(evt);
            }
        });

        txtAPusuario.setEnabled(false);
        txtAPusuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtAPusuarioKeyTyped(evt);
            }
        });

        txtAMusuario.setEnabled(false);
        txtAMusuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtAMusuarioKeyTyped(evt);
            }
        });

        txtNicknameUsuario.setEnabled(false);

        txtPassUsuario.setEnabled(false);

        txtEmailUsuario.setEnabled(false);

        cmbxTipoUsuario.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Admin", "Usuario" }));
        cmbxTipoUsuario.setEnabled(false);

        cmbxAccesoUsuario.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Permitir", "Denegar" }));
        cmbxAccesoUsuario.setEnabled(false);

        btnModificarUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/ModificarS.png"))); // NOI18N
        btnModificarUsuario.setText("Modificar");
        btnModificarUsuario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnModificarUsuario.setEnabled(false);
        btnModificarUsuario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnModificarUsuarioMouseClicked(evt);
            }
        });

        btnGuardarCambiosUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/GuardarCambios.png"))); // NOI18N
        btnGuardarCambiosUsuario.setText("Guardar");
        btnGuardarCambiosUsuario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardarCambiosUsuario.setEnabled(false);
        btnGuardarCambiosUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarCambiosUsuarioActionPerformed(evt);
            }
        });

        btnCancelarCambiosUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Cancelar.png"))); // NOI18N
        btnCancelarCambiosUsuario.setText("Cancelar");
        btnCancelarCambiosUsuario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelarCambiosUsuario.setEnabled(false);
        btnCancelarCambiosUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarCambiosUsuarioActionPerformed(evt);
            }
        });

        cbxMostrarPassUsuario.setEnabled(false);
        cbxMostrarPassUsuario.setLabel("Mostrar contraseña");
        cbxMostrarPassUsuario.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxMostrarPassUsuarioItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jpDetalleUsuarioLayout = new javax.swing.GroupLayout(jpDetalleUsuario);
        jpDetalleUsuario.setLayout(jpDetalleUsuarioLayout);
        jpDetalleUsuarioLayout.setHorizontalGroup(
            jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDetalleUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jpDetalleUsuarioLayout.createSequentialGroup()
                            .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblAPusuario)
                                .addComponent(lblNombreUsuario))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtNombreUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtAPusuario)))
                        .addGroup(jpDetalleUsuarioLayout.createSequentialGroup()
                            .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblAMusuario)
                                .addComponent(lblNicknameUsuario)
                                .addComponent(lblPassUsuario)
                                .addComponent(lblEmailUsuario)
                                .addComponent(lblTipoUsuario)
                                .addComponent(lblAccesoUsuario))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cbxMostrarPassUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtAMusuario)
                                    .addComponent(txtNicknameUsuario)
                                    .addComponent(txtPassUsuario)
                                    .addComponent(txtEmailUsuario)
                                    .addComponent(cmbxTipoUsuario, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbxAccesoUsuario, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(jpDetalleUsuarioLayout.createSequentialGroup()
                        .addGap(113, 113, 113)
                        .addComponent(btnModificarUsuario)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGuardarCambiosUsuario)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelarCambiosUsuario)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpDetalleUsuarioLayout.setVerticalGroup(
            jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDetalleUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombreUsuario)
                    .addComponent(txtNombreUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAPusuario)
                    .addComponent(txtAPusuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAMusuario)
                    .addComponent(txtAMusuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNicknameUsuario)
                    .addComponent(txtNicknameUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassUsuario)
                    .addComponent(txtPassUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxMostrarPassUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmailUsuario)
                    .addComponent(txtEmailUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTipoUsuario)
                    .addComponent(cmbxTipoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAccesoUsuario)
                    .addComponent(cmbxAccesoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jpDetalleUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnModificarUsuario)
                    .addComponent(btnGuardarCambiosUsuario)
                    .addComponent(btnCancelarCambiosUsuario))
                .addContainerGap())
        );

        javax.swing.GroupLayout jpUsuariosLayout = new javax.swing.GroupLayout(jpUsuarios);
        jpUsuarios.setLayout(jpUsuariosLayout);
        jpUsuariosLayout.setHorizontalGroup(
            jpUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpUsuariosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpUsuariosLayout.createSequentialGroup()
                        .addGap(0, 1165, Short.MAX_VALUE)
                        .addComponent(btnAtras2))
                    .addGroup(jpUsuariosLayout.createSequentialGroup()
                        .addComponent(jpVerUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jpDetalleUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jpUsuariosLayout.setVerticalGroup(
            jpUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpUsuariosLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jpUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jpVerUsuarios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jpDetalleUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(btnAtras2)
                .addContainerGap())
        );

        getContentPane().add(jpUsuarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 1270, 580));

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

    private void btnSeñalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSeñalesActionPerformed
        // TODO add your handling code here:
        jpPrincipal.setVisible(false);
        actualizarSeñales();
        jpSeñales.setVisible(true);
    }//GEN-LAST:event_btnSeñalesActionPerformed

    private void btnAtrasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtrasActionPerformed
        // TODO add your handling code here:
        jpPrincipal.setVisible(true);
        jpSeñales.setVisible(false);
        limpiarPaneles("jpSeñales");
    }//GEN-LAST:event_btnAtrasActionPerformed

    private void btnAtras1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtras1ActionPerformed
        // TODO add your handling code here:
        jpPrincipal.setVisible(true);
        jpBitacora.setVisible(false);
        limpiarPaneles("jpBitacora");
    }//GEN-LAST:event_btnAtras1ActionPerformed

    private void btnBitacoraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBitacoraActionPerformed
        // TODO add your handling code here:
        jpPrincipal.setVisible(false);
        jpBitacora.setVisible(true);
    }//GEN-LAST:event_btnBitacoraActionPerformed

    private void btnAtras2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtras2ActionPerformed
        // TODO add your handling code here:
        jpPrincipal.setVisible(true);
        jpUsuarios.setVisible(false);
    }//GEN-LAST:event_btnAtras2ActionPerformed

    private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuariosActionPerformed
        // TODO add your handling code here:
        jpPrincipal.setVisible(false);
        conect = new Conexion();
        conect.obtenerUsuarios(tblUsuarios);
        jpUsuarios.setVisible(true);
        cerrarConexion();
    }//GEN-LAST:event_btnUsuariosActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        // TODO add your handling code here:
        if(!txtNombreImg.getText().isEmpty() && !txtRutaImagen.getText().isEmpty() && !txtRutaAudio.getText().isEmpty()){
            nvaRutaImagen = copiarArchivo(rutaImagen,txtNombreImg.getText(),tipoImagen); //Se copia la imagen al nuevo directorio
            nvaRutaAudio = copiarArchivo(rutaAudio,txtNombreImg.getText(),tipoAudio); //Se copia el audio al nuevo directorio
            
            conect = new Conexion();
            cambiarRutas();
            if(!conect.agregarSeñal(txtNombreImg.getText(), nvaRutaAudio, nvaRutaImagen))
                JOptionPane.showMessageDialog(null,"Hubo un problema al agregar la señal","Error al agregar",JOptionPane.ERROR_MESSAGE);
            else{
                jpAgregar.setVisible(false);
                actualizarSeñales();
                jpSeñales.setVisible(true);
                limpiarPaneles("jpAgregar");
            }
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        // TODO add your handling code here:
        jpSeñales.setVisible(false);
        jpAgregar.setVisible(true);
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        // TODO add your handling code here:
        if(JOptionPane.showConfirmDialog(null, "Realmente deseas cancelar el registro?", "Confirmar salida", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==0){
            jpAgregar.setVisible(false);
            jpSeñales.setVisible(true);
            limpiarPaneles("jpAgregar");
        }
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnImagenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImagenActionPerformed
        // TODO add your handling code here:
        JFileChooser dlg = new JFileChooser();
        dlg.setFileFilter(filterImage);
        int opcion = dlg.showOpenDialog(this);
        if(opcion==JFileChooser.APPROVE_OPTION){
            String fil = dlg.getSelectedFile().getPath();
            lblPrevisualizar.setIcon(new ImageIcon(fil));
            ImageIcon icon = new ImageIcon(fil);
            Image img = icon.getImage();
            Image nvaImg = img.getScaledInstance(426, 361, java.awt.Image.SCALE_SMOOTH);
            ImageIcon nvoIcon = new ImageIcon(nvaImg);
            lblPrevisualizar.setIcon(nvoIcon);
            txtRutaImagen.setText(fil);
            rutaImagen = fil;
            //System.out.println(dlg.getSelectedFile().getParent());
        }
    }//GEN-LAST:event_btnImagenActionPerformed

    private void btnAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAudioActionPerformed
        // TODO add your handling code here:
        JFileChooser dlg = new JFileChooser();
        dlg.setFileFilter(filterAudio);
        int opcion = dlg.showOpenDialog(this);
        if(opcion==JFileChooser.APPROVE_OPTION){
            String file = dlg.getSelectedFile().getPath();
            txtRutaAudio.setText(file);
            rutaAudio = file;
        }
    }//GEN-LAST:event_btnAudioActionPerformed

    private void txtNombreImgKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNombreImgKeyReleased
        // TODO add your handling code here:
        if(txtNombreImg.getText().length()!=0 && !conect.señalDuplicada(txtNombreImg.getText())){
            btnImagen.setEnabled(true);
            btnAudio.setEnabled(true);
        }else{
            txtNombreImg.setText(null);
            btnImagen.setEnabled(false);
            btnAudio.setEnabled(false);
        }
    }//GEN-LAST:event_txtNombreImgKeyReleased

    private void tblSeñalesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSeñalesMouseClicked
        // TODO add your handling code here:
        seleccion = tblSeñales.rowAtPoint(evt.getPoint());
        String ruta = String.valueOf(tblSeñales.getValueAt(seleccion,2));
        btnEliminar.setEnabled(true);
        File file = new File(ruta);
        if(!file.exists()){
            ruta = System.getProperty("user.dir")+"\\default.jpg";
        }
        txtNombreM.setText(String.valueOf(tblSeñales.getValueAt(seleccion,1)));
        txtRutaA.setText(String.valueOf(tblSeñales.getValueAt(seleccion,3)));
        lblImagenS.setIcon(redimensionarImagen(ruta,387,361));  
        btnModificar.setEnabled(true);
    }//GEN-LAST:event_tblSeñalesMouseClicked

    private void rbtnFiltroFechaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtnFiltroFechaActionPerformed
        // TODO add your handling code here:
        //Panel fecha
        jpFFecha.setEnabled(true);
        lblFecha.setEnabled(true);
        jdFecha.setEnabled(true);
        //Panel usuario
        jpFUsuario.setEnabled(false);
        lblNUsuario.setEnabled(false);
        cbxUsuario.setEnabled(false);
        //Panel señal
        jpFSeñal.setEnabled(false);
        lblSeñal.setEnabled(false);
        cbxSeñal.setEnabled(false);
    }//GEN-LAST:event_rbtnFiltroFechaActionPerformed

    private void rbtnFiltroTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtnFiltroTodosActionPerformed
        // TODO add your handling code here:
        //Panel fecha
        jpFFecha.setEnabled(false);
        lblFecha.setEnabled(false);
        jdFecha.setEnabled(false);
        //Panel usuario
        jpFUsuario.setEnabled(false);
        lblNUsuario.setEnabled(false);
        cbxUsuario.setEnabled(false);
        //Panel señal
        jpFSeñal.setEnabled(false);
        lblSeñal.setEnabled(false);
        cbxSeñal.setEnabled(false);
    }//GEN-LAST:event_rbtnFiltroTodosActionPerformed

    private void rbtnFiltroUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtnFiltroUsuarioActionPerformed
        // TODO add your handling code here:
        //Panel fecha
        jpFFecha.setEnabled(false);
        lblFecha.setEnabled(false);
        jdFecha.setEnabled(false);
        //Panel usuario
        jpFUsuario.setEnabled(true);
        lblNUsuario.setEnabled(true);
        cbxUsuario.setEnabled(true);
        //Panel señal
        jpFSeñal.setEnabled(false);
        lblSeñal.setEnabled(false);
        cbxSeñal.setEnabled(false);
    }//GEN-LAST:event_rbtnFiltroUsuarioActionPerformed

    private void rbtnFiltroSeñalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtnFiltroSeñalActionPerformed
        // TODO add your handling code here:
        //Panel fecha
        jpFFecha.setEnabled(false);
        lblFecha.setEnabled(false);
        jdFecha.setEnabled(false);
        //Panel usuario
        jpFUsuario.setEnabled(false);
        lblNUsuario.setEnabled(false);
        cbxUsuario.setEnabled(false);
        //Panel señal
        jpFSeñal.setEnabled(true);
        lblSeñal.setEnabled(true);
        cbxSeñal.setEnabled(true);
    }//GEN-LAST:event_rbtnFiltroSeñalActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        // TODO add your handling code here:
        conect = new Conexion();
        if(rbtnFiltroTodos.isSelected()){
            conect.obtenerBitacora(tblBitacora,"","","","");
        }
        if(rbtnFiltroFecha.isSelected()){
            if(jdFecha.getDate()!=null){
                conect.obtenerBitacora(tblBitacora,"Fecha",cambiarFormatoFecha(),"","");
            }else{
                JOptionPane.showMessageDialog(null,"Debes seleccionar primero una fecha","Fecha incorrecta",JOptionPane.ERROR_MESSAGE);
            }
        }
        if(rbtnFiltroUsuario.isSelected()){
            conect.obtenerBitacora(tblBitacora,"Usuario","",cbxUsuario.getSelectedItem().toString(),"");
        }
        if(rbtnFiltroSeñal.isSelected()){
            conect.obtenerBitacora(tblBitacora,"Señal","","",cbxSeñal.getSelectedItem().toString());
        }
        cerrarConexion();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void tblBitacoraMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBitacoraMouseClicked
        // TODO add your handling code here:
        seleccion = tblBitacora.rowAtPoint(evt.getPoint());
        String ruta = String.valueOf(tblBitacora.getValueAt(seleccion,5));
        File file = new File(ruta);
        if(file.exists()){
            ruta = String.valueOf(tblBitacora.getValueAt(seleccion,5));
        }else{
            ruta = System.getProperty("user.dir")+"\\default.jpg";
        }
        txtNombreS.setText(String.valueOf(tblBitacora.getValueAt(seleccion,4)));
        lblVerSeñal.setIcon(redimensionarImagen(ruta,240,244));  
    }//GEN-LAST:event_tblBitacoraMouseClicked

    private void btnGuardarCambiosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarCambiosActionPerformed
        // TODO add your handling code here:
        if(!txtNombreM.getText().isEmpty()){
            if(!rutaAudio.isEmpty()){
                nvaRutaImagen = "";
                eliminarUrlAudio(); //Se elimina el audio contenido en la carpeta
                nvaRutaAudio = copiarArchivo(rutaAudio,txtNombreM.getText(),tipoAudio); //Se copia el audio al nuevo directorio
                conect = new Conexion();
                cambiarRutas();
            }
            if(!conect.modificarSeñal(Integer.parseInt(String.valueOf(tblSeñales.getValueAt(seleccion,0))), txtNombreM.getText(), nvaRutaAudio)){
                JOptionPane.showMessageDialog(null,"Hubo un problema al modificar los datos","Error al modificar",JOptionPane.ERROR_MESSAGE);
            }else{
                habilitarBotonesPanelSeñales("Guardar");
            }  
            cerrarConexion();
        }
    }//GEN-LAST:event_btnGuardarCambiosActionPerformed

    public void eliminarUrlAudio(){
        if(seleccion>=0){
            String url = String.valueOf(tblSeñales.getValueAt(seleccion,3));
            File file = new File(url);
            if(file.exists()){
                file.delete();
            }
        }
    }
    
    private void btnModificarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnModificarMouseClicked
        // TODO add your handling code here:
        rutaAudio = "";
        txtNombreM.setEnabled(true);
        btnAudioM.setEnabled(true);
        btnGuardarCambios.setEnabled(true);
        btnCancelarCambios.setEnabled(true);
        btnModificar.setEnabled(false);
        btnAgregar.setEnabled(false);
    }//GEN-LAST:event_btnModificarMouseClicked

    private void btnAudioMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAudioMActionPerformed
        // TODO add your handling code here:
        JFileChooser dlg = new JFileChooser();
        dlg.setFileFilter(filterAudio);
        int opcion = dlg.showOpenDialog(this);
        if(opcion==JFileChooser.APPROVE_OPTION){
            String file = dlg.getSelectedFile().getPath();
            txtRutaAudio.setText(file);
            rutaAudio = file;
            //System.out.println(dlg.getSelectedFile().getParent());
        }
    }//GEN-LAST:event_btnAudioMActionPerformed

    private void btnCancelarCambiosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarCambiosActionPerformed
        // TODO add your handling code here:
        if(JOptionPane.showConfirmDialog(null, "Realmente deseas cancelar los cambios?", "Confirmar salida", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==0){
            habilitarBotonesPanelSeñales("");
        }
    }//GEN-LAST:event_btnCancelarCambiosActionPerformed

    private void txtNombreMFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNombreMFocusLost
        // TODO add your handling code here:
        conect = new Conexion();
        if(conect.señalDuplicada(txtNombreM.getText())){
            txtNombreM.setText(null);
        }
    }//GEN-LAST:event_txtNombreMFocusLost

    private void txtNombreMKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNombreMKeyReleased
        // TODO add your handling code here:
        conect = new Conexion();
        if(conect.señalDuplicada(txtNombreM.getText())){
            txtNombreM.setText(null);
        }
    }//GEN-LAST:event_txtNombreMKeyReleased

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        // TODO add your handling code here:
        if(seleccion>=0){
            if(JOptionPane.showConfirmDialog(null, "Realmente deseas eliminar la señal?", "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==0){
                if(!conect.eliminarSeñal(Integer.parseInt(String.valueOf(tblSeñales.getValueAt(seleccion,0))))){
                    JOptionPane.showMessageDialog(null,"Hubo un problema al eliminar la señal","Error al eliminar",JOptionPane.ERROR_MESSAGE);
                }else{
                    lblImagenS.setIcon(new ImageIcon(""));
                    txtNombreS.setText("");
                    txtNombreM.setText("");
                    txtRutaA.setText("");
                    habilitarBotonesPanelSeñales("Eliminar");
                }
            }
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void tblUsuariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUsuariosMouseClicked
        // TODO add your handling code here:
        seleccion = tblUsuarios.rowAtPoint(evt.getPoint());
        if(seleccion>=0){
            String nicknameUsr = String.valueOf(tblUsuarios.getValueAt(seleccion,1));
            Usuario usr = new Usuario();
            conect = new Conexion();
            usr = conect.buscarUsr(nicknameUsr);
            txtNombreUsuario.setText(usr.getNombre());
            txtAPusuario.setText(usr.getaPaterno());
            txtAMusuario.setText(usr.getaMaterno());
            txtNicknameUsuario.setText(usr.getNickname());
            txtPassUsuario.setText(usr.getPassword());
            txtEmailUsuario.setText(usr.getEmail());
            cmbxTipoUsuario.setSelectedIndex((usr.getTipo()=="admin")?0:1);
            cmbxAccesoUsuario.setSelectedIndex((usr.getActivo()==true)?0:1);
            btnModificarUsuario.setEnabled(true);
        }
    }//GEN-LAST:event_tblUsuariosMouseClicked

    private void btnModificarUsuarioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnModificarUsuarioMouseClicked
        // TODO add your handling code here:
        btnModificarUsuario.setEnabled(false);
        btnCancelarCambiosUsuario.setEnabled(true);
        btnGuardarCambiosUsuario.setEnabled(true);
        txtNombreUsuario.setEnabled(true);
        txtAPusuario.setEnabled(true);
        txtAMusuario.setEnabled(true);
        txtPassUsuario.setEnabled(true);
        cmbxTipoUsuario.setEnabled(true);
        cmbxAccesoUsuario.setEnabled(true);
        cbxMostrarPassUsuario.setEnabled(true);
    }//GEN-LAST:event_btnModificarUsuarioMouseClicked

    private void btnGuardarCambiosUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarCambiosUsuarioActionPerformed
        // TODO add your handling code here:
        if(!txtNombreUsuario.getText().isEmpty() && !txtAPusuario.getText().isEmpty() && !txtAPusuario.getText().isEmpty() && !txtPassUsuario.getText().isEmpty()){
            Usuario usr = new Usuario();
            usr.setNickname(String.valueOf(tblUsuarios.getValueAt(seleccion,1)));
            usr.setNombre(txtNombreUsuario.getText());
            usr.setaMaterno(txtAMusuario.getText());
            usr.setaPaterno(txtAPusuario.getText());
            usr.setPassword(txtPassUsuario.getText());
            usr.setTipo((cmbxTipoUsuario.getSelectedIndex()==0)?"admin":"usuario");
            usr.setActivo((cmbxAccesoUsuario.getSelectedIndex()==0)?true:false);
            usr.setEmail(txtEmailUsuario.getText());
            conect = new Conexion();
            if(!conect.modificarUsuario(String.valueOf(tblUsuarios.getValueAt(seleccion,1)),usr)){
                JOptionPane.showMessageDialog(null,"Hubo un problema al modificar los datos","Error al modificar",JOptionPane.ERROR_MESSAGE);
            }else{
                btnCancelarCambiosUsuario.setEnabled(false);
                btnGuardarCambiosUsuario.setEnabled(false);
                btnModificarUsuario.setEnabled(true);
                txtNombreUsuario.setEnabled(false);
                txtAPusuario.setEnabled(false);
                txtAMusuario.setEnabled(false);
                txtPassUsuario.setEnabled(false);
                cmbxTipoUsuario.setEnabled(false);
                cmbxAccesoUsuario.setEnabled(false);
                cbxMostrarPassUsuario.setEnabled(false);
                limpiarPaneles("jpUsuarios");
            }
            cerrarConexion();
        }else{
            JOptionPane.showMessageDialog(null,"Debes de llenar todos los campos","Campos vacios",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarCambiosUsuarioActionPerformed

    private void btnCancelarCambiosUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarCambiosUsuarioActionPerformed
        // TODO add your handling code here:
        if(JOptionPane.showConfirmDialog(null, "Realmente deseas cancelar los cambios?", "Confirmar salida", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==0){
            btnCancelarCambiosUsuario.setEnabled(false);
            btnGuardarCambiosUsuario.setEnabled(false);
            btnModificarUsuario.setEnabled(true);
            txtNombreUsuario.setEnabled(false);
            txtAPusuario.setEnabled(false);
            txtAMusuario.setEnabled(false);
            txtPassUsuario.setEnabled(false);
            cmbxTipoUsuario.setEnabled(false);
            cmbxAccesoUsuario.setEnabled(false);
            cbxMostrarPassUsuario.setEnabled(false);
            limpiarPaneles("jpUsuarios");
        }
    }//GEN-LAST:event_btnCancelarCambiosUsuarioActionPerformed

    private void cbxMostrarPassUsuarioItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxMostrarPassUsuarioItemStateChanged
        // TODO add your handling code here:
        char i = txtPassUsuario.getEchoChar();
        if (cbxMostrarPassUsuario.getState()) {  // a es una variable boolean en true
            txtPassUsuario.setEchoChar((char)0); // este método es el que hace visible el texto del jPasswordField
        } else {
            txtPassUsuario.setEchoChar('*'); // i es el char
        }
    }//GEN-LAST:event_cbxMostrarPassUsuarioItemStateChanged

    private void txtNombreUsuarioKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNombreUsuarioKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if(Character.isDigit(c)){
            JOptionPane.showMessageDialog(this,"No se pueden ingresar digitos en este campo","Error de tipos",JOptionPane.INFORMATION_MESSAGE);
            evt.consume();
        }
    }//GEN-LAST:event_txtNombreUsuarioKeyTyped

    private void txtAPusuarioKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAPusuarioKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if(Character.isDigit(c)){
            JOptionPane.showMessageDialog(this,"No se pueden ingresar digitos en este campo","Error de tipos",JOptionPane.INFORMATION_MESSAGE);
            evt.consume();
        }
    }//GEN-LAST:event_txtAPusuarioKeyTyped

    private void txtAMusuarioKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAMusuarioKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if(Character.isDigit(c)){
            JOptionPane.showMessageDialog(this,"No se pueden ingresar digitos en este campo","Error de tipos",JOptionPane.INFORMATION_MESSAGE);
            evt.consume();
        }
    }//GEN-LAST:event_txtAMusuarioKeyTyped

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

    public ImageIcon redimensionarImagen(String ruta, int hTam, int vTam){
        ImageIcon icon = new ImageIcon(ruta);
        Image img = icon.getImage();
        Image nvaImg = img.getScaledInstance(hTam, vTam, java.awt.Image.SCALE_SMOOTH);
        ImageIcon nvoIcon = new ImageIcon(nvaImg);
        return nvoIcon;
    }
    /**
     * @param args the command line arguments
     */
    /*
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmAdmin("").setVisible(true);
            }
        });
    }*/

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgrpBusqueda;
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnAtras;
    private javax.swing.JButton btnAtras1;
    private javax.swing.JButton btnAtras2;
    private javax.swing.JButton btnAudio;
    private javax.swing.JButton btnAudioM;
    private javax.swing.JButton btnBitacora;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCancelarCambios;
    private javax.swing.JButton btnCancelarCambiosU;
    private javax.swing.JButton btnCancelarCambiosUsuario;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnGuardarCambios;
    private javax.swing.JButton btnGuardarCambiosU;
    private javax.swing.JButton btnGuardarCambiosUsuario;
    private javax.swing.JButton btnImagen;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnModificarUsuario;
    private javax.swing.JButton btnSalir;
    private javax.swing.JButton btnSeñales;
    private javax.swing.JButton btnUsuarios;
    private java.awt.Checkbox cbxMostrarPassUM;
    private java.awt.Checkbox cbxMostrarPassUsuario;
    private javax.swing.JComboBox<String> cbxSeñal;
    private javax.swing.JComboBox<String> cbxUsuario;
    private javax.swing.JComboBox<String> cmbxAccesoUsuario;
    private javax.swing.JComboBox<String> cmbxTipoUsuario;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private com.toedter.calendar.JDateChooser jdFecha;
    private javax.swing.JPanel jpAgregar;
    private javax.swing.JPanel jpBitacora;
    private javax.swing.JPanel jpDatosAudio;
    private javax.swing.JPanel jpDatosSeñal;
    private javax.swing.JPanel jpDatosUsuario;
    private javax.swing.JPanel jpDescripcion;
    private javax.swing.JPanel jpDetalleUsuario;
    private javax.swing.JPanel jpFFecha;
    private javax.swing.JPanel jpFSeñal;
    private javax.swing.JPanel jpFUsuario;
    private javax.swing.JPanel jpFiltros;
    private javax.swing.JPanel jpImagen;
    private javax.swing.JPanel jpModificar;
    private javax.swing.JPanel jpPrincipal;
    private javax.swing.JPanel jpRegistros;
    private javax.swing.JPanel jpSeñales;
    private javax.swing.JPanel jpUsuarios;
    private javax.swing.JPanel jpVerUsuarios;
    private javax.swing.JLabel lblAMu;
    private javax.swing.JLabel lblAMusuario;
    private javax.swing.JLabel lblAPu;
    private javax.swing.JLabel lblAPusuario;
    private javax.swing.JLabel lblAccesoUsuario;
    private javax.swing.JLabel lblConPassU;
    private javax.swing.JLabel lblEmailU;
    private javax.swing.JLabel lblEmailUsuario;
    private javax.swing.JLabel lblEtiquetaUsr;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFondo;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblIcono;
    private javax.swing.JLabel lblImagenS;
    private javax.swing.JLabel lblNUsuario;
    private javax.swing.JLabel lblNicknameU;
    private javax.swing.JLabel lblNicknameUsuario;
    private javax.swing.JLabel lblNombreImg;
    private javax.swing.JLabel lblNombreM;
    private javax.swing.JLabel lblNombreS;
    private javax.swing.JLabel lblNombreU;
    private javax.swing.JLabel lblNombreUsuario;
    private javax.swing.JLabel lblPassU;
    private javax.swing.JLabel lblPassUsuario;
    private javax.swing.JLabel lblPrevisualizar;
    private javax.swing.JLabel lblRutaA;
    private javax.swing.JLabel lblRutaImagen;
    private javax.swing.JLabel lblRutaImagen1;
    private javax.swing.JLabel lblSeñal;
    private javax.swing.JLabel lblTipoUsuario;
    private javax.swing.JLabel lblUser;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblVerSeñal;
    private javax.swing.JRadioButton rbtnFiltroFecha;
    private javax.swing.JRadioButton rbtnFiltroSeñal;
    private javax.swing.JRadioButton rbtnFiltroTodos;
    private javax.swing.JRadioButton rbtnFiltroUsuario;
    private javax.swing.JTable tblBitacora;
    private javax.swing.JTable tblSeñales;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JTextField txtAMu;
    private javax.swing.JTextField txtAMusuario;
    private javax.swing.JTextField txtAPu;
    private javax.swing.JTextField txtAPusuario;
    private javax.swing.JPasswordField txtConPassU;
    private javax.swing.JTextField txtEmailU;
    private javax.swing.JTextField txtEmailUsuario;
    private javax.swing.JTextField txtNicknameU;
    private javax.swing.JTextField txtNicknameUsuario;
    private javax.swing.JTextField txtNombreImg;
    private javax.swing.JTextField txtNombreM;
    private javax.swing.JTextField txtNombreS;
    private javax.swing.JTextField txtNombreU;
    private javax.swing.JTextField txtNombreUsuario;
    private javax.swing.JPasswordField txtPassU;
    private javax.swing.JPasswordField txtPassUsuario;
    private javax.swing.JTextField txtRutaA;
    private javax.swing.JTextField txtRutaAudio;
    private javax.swing.JTextField txtRutaImagen;
    // End of variables declaration//GEN-END:variables
}
