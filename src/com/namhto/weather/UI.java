/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namhto.weather;

import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author othman
 */
public class UI extends javax.swing.JFrame {

    private Point firstClick;
    private Weather w;
    private String geoLocation = "";
    private double lat = 0.0;
    private double lon = 0.0;
    
    private boolean ignoreEvents = false;
    /**
     * Creates new form UI
     */
    public UI() throws FileNotFoundException, IOException, ParseException, java.text.ParseException {
        initComponents();
        
        BufferedReader br = new BufferedReader(new FileReader(getClass().getResource("/com/namhto/weather/lastPos.txt").getPath()));
        Object[] str = br.lines().toArray();
        
        if(str.length == 2)
            this.setLocation(Integer.parseInt((String)str[0]), Integer.parseInt((String)str[1]));
        else
            this.setLocation((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2 - this.getSize().width/2, (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2 - this.getSize().height/2);
        populate();
    }
    
    public void populate() throws FileNotFoundException, IOException, ParseException, java.text.ParseException {
        
        String json = connection();
        this.w = new Weather(json);
        this.w.parse();
        
        this.city.setText(this.w.getCity()+", "+this.w.getCountry());
        this.temp.setText(String.valueOf(this.w.ls.get(0).getTemp())+"° c");
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        this.date.setText(format.format(this.w.ls.get(0).getDate()));
        this.sky.setText(this.w.ls.get(0).getSky());
        
        String s = this.w.ls.get(0).getSky();
        
        BufferedImage bigImg = ImageIO.read(getClass().getResource("/com/namhto/images/map.png"));

        final int width = 96;
        final int height = 90;
        final int rows = 7;
        final int cols = 5;
        BufferedImage[] sprites = new BufferedImage[rows * cols];

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                sprites[(i * cols) + j] = bigImg.getSubimage(j * width,i * height,width,height);
            }
        }
        
        this.refresh.setIcon(new ImageIcon(sprites[18]));
        
        int hour = Calendar.HOUR_OF_DAY;
        
        switch(s){
            case "Rain":
                this.weather.setIcon(new ImageIcon(sprites[26]));
                break;
            case "Clear":
                if(hour >= 6 && hour < 18)
                    this.weather.setIcon(new ImageIcon(sprites[5]));
                else
                    this.weather.setIcon(new ImageIcon(sprites[10]));
                break;
            case "Clouds":
                if(hour >= 6 && hour < 18)
                    this.weather.setIcon(new ImageIcon(sprites[9]));
                else
                    this.weather.setIcon(new ImageIcon(sprites[14]));
                break;
            case "Snow":
                this.weather.setIcon(new ImageIcon(sprites[32]));
                break;       
            default:
                break;
        }
    }
    
    public void getCoords() throws ParseException {
        
        JSONParser parser = new JSONParser();
        JSONObject jsonobject = (JSONObject) parser.parse(this.geoLocation);
        this.lat = (Double) jsonobject.get("lat");
        this.lon = (Double) jsonobject.get("lon");
    }

    public String connection() throws IOException, java.text.ParseException, ParseException {
        
        /******************************************IP geolocation**********************************************/
        String geolocationResponse ="";
        URL geolocationURL = new URL("http://ip-api.com/json");
        URLConnection geolocationConnection = geolocationURL.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(geolocationConnection.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            geolocationResponse += inputLine;
        in.close();
        
        this.geoLocation = geolocationResponse;
        
        getCoords();
        
        /******************************************Saving the last call time to the API**********************************************/
        BufferedReader br = new BufferedReader(new FileReader(getClass().getResource("/com/namhto/weather/count.txt").getPath()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        
        Long dt = 0l;
        String s = br.readLine();
        if(s != null) {
            Date parsedDate = dateFormat.parse(s);
            Timestamp lastCall = new Timestamp(parsedDate.getTime());
            dt = new Timestamp(new Date().getTime()).getTime() - lastCall.getTime();
            
            if(dt < 600000) { //If last call time less than 10 minutes, use the saved previous response, no call
            BufferedReader br2 = new BufferedReader(new FileReader(getClass().getResource("/com/namhto/weather/save.txt").getPath()));
            return br2.readLine();
            }

            else {
                String weatherResponse ="";
                String requestURL = "http://api.openweathermap.org/data/2.5/weather?lat=" + this.lat + "&lon=" + this.lon + "&APPID=06ffd4bac466fcc023a7d2a824aa2891";
                URL oracle = new URL(requestURL);
                URLConnection yc = oracle.openConnection();
                BufferedReader in2 = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine2;

                while ((inputLine2 = in2.readLine()) != null)
                    weatherResponse +=inputLine2;
                in2.close();

                /******************************************Saving the counter since the last call to the weather API**********************************************/
                BufferedWriter out = new BufferedWriter( new FileWriter(getClass().getResource("/com/namhto/weather/count.txt").getPath()));
                out.write(new Timestamp(new Date().getTime()).toString());
                out.close();

                /******************************************Saving the last response of the weather API**********************************************/
                BufferedWriter out2 = new BufferedWriter( new FileWriter(getClass().getResource("/com/namhto/weather/save.txt").getPath()));
                out2.write(weatherResponse);
                out2.close();

                return weatherResponse;
            }
        }
        else {
            
            String weatherResponse ="";
                String requestURL = "http://api.openweathermap.org/data/2.5/weather?lat=" + this.lat + "&lon=" + this.lon + "&APPID=06ffd4bac466fcc023a7d2a824aa2891";
                URL oracle = new URL(requestURL);
                URLConnection yc = oracle.openConnection();
                BufferedReader in2 = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine2;

                while ((inputLine2 = in2.readLine()) != null)
                    weatherResponse +=inputLine2;
                in2.close();

                /******************************************Saving the counter since the last call to the weather API**********************************************/
                BufferedWriter out = new BufferedWriter( new FileWriter(getClass().getResource("/com/namhto/weather/count.txt").getPath()));
                out.write(new Timestamp(new Date().getTime()).toString());
                out.close();

                /******************************************Saving the last response of the weather API**********************************************/
                BufferedWriter out2 = new BufferedWriter( new FileWriter(getClass().getResource("/com/namhto/weather/save.txt").getPath()));
                out2.write(weatherResponse);
                out2.close();

                return weatherResponse;
        }
        /******************************************Weather API call**********************************************/
        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        exit = new javax.swing.JLabel();
        refresh = new javax.swing.JLabel();
        refreshFeedBack = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        weather = new javax.swing.JLabel();
        sky = new javax.swing.JLabel();
        date = new javax.swing.JLabel();
        temp = new javax.swing.JLabel();
        first = new javax.swing.JLabel();
        city = new javax.swing.JLabel();
        menu = new javax.swing.JLabel();
        background = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        exit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        exit.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                exitMouseMoved(evt);
            }
        });
        exit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                exitMouseReleased(evt);
            }
        });
        getContentPane().add(exit, new org.netbeans.lib.awtextra.AbsoluteConstraints(251, 0, 30, 30));

        refresh.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                refreshMouseReleased(evt);
            }
        });
        getContentPane().add(refresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 50, 60, 50));

        refreshFeedBack.setFont(new java.awt.Font("Calibri", 0, 36)); // NOI18N
        refreshFeedBack.setForeground(new java.awt.Color(255, 255, 255));
        refreshFeedBack.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        refreshFeedBack.setMaximumSize(new java.awt.Dimension(283, 255));
        refreshFeedBack.setMinimumSize(new java.awt.Dimension(283, 255));
        refreshFeedBack.setPreferredSize(new java.awt.Dimension(283, 255));
        getContentPane().add(refreshFeedBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 283, 224));

        jLabel1.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Live Coding Weather app - Namhto");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 210, 30));

        weather.setFont(new java.awt.Font("Calibri", 0, 36)); // NOI18N
        weather.setForeground(new java.awt.Color(255, 255, 255));
        getContentPane().add(weather, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 100, 120, 110));

        sky.setFont(new java.awt.Font("Calibri", 0, 20)); // NOI18N
        sky.setForeground(new java.awt.Color(255, 255, 255));
        getContentPane().add(sky, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 120, 30));

        date.setFont(new java.awt.Font("Calibri", 0, 18)); // NOI18N
        date.setForeground(new java.awt.Color(255, 255, 255));
        getContentPane().add(date, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 260, 30));

        temp.setFont(new java.awt.Font("Calibri", 0, 48)); // NOI18N
        temp.setForeground(new java.awt.Color(255, 255, 255));
        getContentPane().add(temp, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 130, 60));

        first.setBackground(new java.awt.Color(46, 204, 113));
        first.setOpaque(true);
        getContentPane().add(first, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 283, 154));

        city.setFont(new java.awt.Font("Calibri", 0, 24)); // NOI18N
        city.setForeground(new java.awt.Color(255, 255, 255));
        getContentPane().add(city, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 200, 50));

        menu.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                menuMouseDragged(evt);
            }
        });
        menu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                menuMousePressed(evt);
            }
        });
        getContentPane().add(menu, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 280, 30));

        background.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/namhto/images/UI.jpg"))); // NOI18N
        getContentPane().add(background, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitMouseReleased
        BufferedWriter out = null;
        try {
            int lastX = this.getLocationOnScreen().x;
            int lastY = this.getLocationOnScreen().y;
            out = new BufferedWriter( new FileWriter(getClass().getResource("/com/namhto/weather/lastPos.txt").getPath()));
            out.write(lastX + "\n" + lastY);
            out.close();
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_exitMouseReleased

    private void exitMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitMouseMoved

    }//GEN-LAST:event_exitMouseMoved

    private void menuMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuMouseDragged
        int dx = evt.getX() - this.firstClick.x;
        int dy = evt.getY() - this.firstClick.y;
        this.setLocation(this.getLocation().x + dx, this.getLocation().y + dy);
    }//GEN-LAST:event_menuMouseDragged

    private void menuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuMousePressed
        this.firstClick = evt.getPoint();
    }//GEN-LAST:event_menuMousePressed

    private void refreshMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshMouseReleased
        
        try {
            if(this.refresh.isEnabled()){
                
                this.refresh.setEnabled(false);
                
                this.refreshFeedBack.setOpaque(true);
                this.refreshFeedBack.setBackground(new Color(100, 100, 100, 200));
                this.refreshFeedBack.setText("Refresh...");
                this.update(this.getGraphics());
                
                long start = System.currentTimeMillis();
                long end = start + 2000;
                while (System.currentTimeMillis() < end)
                {
                }

                populate();
                this.refreshFeedBack.setOpaque(false);
                this.refreshFeedBack.setText("");
                
                this.refresh.setEnabled(true);
            }           
           
        } catch (IOException ex) {
            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (java.text.ParseException ex) {
            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_refreshMouseReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new UI().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.text.ParseException ex) {
                    Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel background;
    private javax.swing.JLabel city;
    private javax.swing.JLabel date;
    private javax.swing.JLabel exit;
    private javax.swing.JLabel first;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel menu;
    private javax.swing.JLabel refresh;
    private javax.swing.JLabel refreshFeedBack;
    private javax.swing.JLabel sky;
    private javax.swing.JLabel temp;
    private javax.swing.JLabel weather;
    // End of variables declaration//GEN-END:variables
}
