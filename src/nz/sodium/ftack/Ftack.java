
package nz.sodium.ftack;

import nz.sodium.*;
import nz.sodium.time.*;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;
import com.jogamp.opengl.util.Animator;

public class Ftack implements GLEventListener {
  private float view_rotx = 20.0f, view_roty = 30.0f;
  private final float view_rotz = 0.0f;
  private int gear1=0, gear2=0, gear3=0;
  private float angle = 0.0f;
  private final int swapInterval;

  private int prevMouseX, prevMouseY;

  public static void main(String[] args) {
    // set argument 'NotFirstUIActionOnProcess' in the JNLP's application-desc tag for example
    // <application-desc main-class="demos.j2d.TextCube"/>
    //   <argument>NotFirstUIActionOnProcess</argument>
    // </application-desc>
    // boolean firstUIActionOnProcess = 0==args.length || !args[0].equals("NotFirstUIActionOnProcess") ;

    java.awt.Frame frame = new java.awt.Frame("Ftack");
    frame.setSize(1200, 700);
    frame.setLayout(new java.awt.BorderLayout());

    final Animator animator = new Animator();
    frame.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
		public void windowClosing(java.awt.event.WindowEvent e) {
          // Run this on another thread than the AWT event queue to
          // make sure the call to Animator.stop() completes before
          // exiting
          new Thread(new Runnable() {
              @Override
			public void run() {
                animator.stop();
                System.exit(0);
              }
            }).start();
        }
      });

    GLCanvas canvas = new GLCanvas();
    animator.add(canvas);
    // GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
    // GLCanvas canvas = new GLCanvas(caps);

    final Ftack gears = new Ftack();
    canvas.addGLEventListener(gears);

    frame.add(canvas, java.awt.BorderLayout.CENTER);
    frame.validate();

    frame.setVisible(true);
    animator.start();
  }

  public Ftack(int swapInterval) {
    this.swapInterval = swapInterval;
  }

  public Ftack() {
    this.swapInterval = 1;
  }

  public void setGears(int g1, int g2, int g3) {
      gear1 = g1;
      gear2 = g2;
      gear3 = g3;
  }

  /**
   * @return display list gear1
   */
  public int getGear1() { return gear1; }

  /**
   * @return display list gear2
   */
  public int getGear2() { return gear2; }

  /**
   * @return display list gear3
   */
  public int getGear3() { return gear3; }

  @Override
public void init(GLAutoDrawable drawable) {
    System.err.println("Gears: Init: "+drawable);
    // Use debug pipeline
    // drawable.setGL(new DebugGL(drawable.getGL()));

    GL2 gl = drawable.getGL().getGL2();

    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    System.err.println("INIT GL IS: " + gl.getClass().getName());
    System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
    System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
    System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));

    float pos[] = { 5.0f, 5.0f, 10.0f, 0.0f };
    float red[] = { 0.8f, 0.1f, 0.0f, 0.7f };
    float green[] = { 0.0f, 0.8f, 0.2f, 0.7f };
    float blue[] = { 0.2f, 0.2f, 1.0f, 0.7f };

    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
    gl.glEnable(GL2.GL_CULL_FACE);
    gl.glEnable(GL2.GL_LIGHTING);
    gl.glEnable(GL2.GL_LIGHT0);
    gl.glEnable(GL2.GL_DEPTH_TEST);

    /* make the gears */
    if(0>=gear1) {
        gear1 = gl.glGenLists(1);
        gl.glNewList(gear1, GL2.GL_COMPILE);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, red, 0);
        gear(gl, 1.0f, 4.0f, 1.0f, 20, 0.7f);
        gl.glEndList();
        System.err.println("gear1 list created: "+gear1);
    } else {
        System.err.println("gear1 list reused: "+gear1);
    }

    if(0>=gear2) {
        gear2 = gl.glGenLists(1);
        gl.glNewList(gear2, GL2.GL_COMPILE);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, green, 0);
        gear(gl, 0.5f, 2.0f, 2.0f, 10, 0.7f);
        gl.glEndList();
        System.err.println("gear2 list created: "+gear2);
    } else {
        System.err.println("gear2 list reused: "+gear2);
    }

    if(0>=gear3) {
        gear3 = gl.glGenLists(1);
        gl.glNewList(gear3, GL2.GL_COMPILE);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, blue, 0);
        gear(gl, 1.3f, 2.0f, 0.5f, 10, 0.7f);
        gl.glEndList();
        System.err.println("gear3 list created: "+gear3);
    } else {
        System.err.println("gear3 list reused: "+gear3);
    }

    gl.glEnable(GL2.GL_NORMALIZE);

    // MouseListener gearsMouse = new TraceMouseAdapter(new GearsMouseAdapter());
    MouseListener gearsMouse = new GearsMouseAdapter();
    KeyListener gearsKeys = new GearsKeyAdapter();

    if (drawable instanceof Window) {
        Window window = (Window) drawable;
        window.addMouseListener(gearsMouse);
        window.addKeyListener(gearsKeys);
    } else if (GLProfile.isAWTAvailable() && drawable instanceof java.awt.Component) {
        java.awt.Component comp = (java.awt.Component) drawable;
        new AWTMouseAdapter(gearsMouse, drawable).addTo(comp);
        new AWTKeyAdapter(gearsKeys, drawable).addTo(comp);
    }
  }

  @Override
public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    System.err.println("Gears: Reshape "+x+"/"+y+" "+width+"x"+height);
    GL2 gl = drawable.getGL().getGL2();

    gl.setSwapInterval(swapInterval);

    float h = (float)height / (float)width;

    gl.glMatrixMode(GL2.GL_PROJECTION);

    gl.glLoadIdentity();
    gl.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 60.0f);
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadIdentity();
    gl.glTranslatef(0.0f, 0.0f, -40.0f);
  }

  @Override
public void dispose(GLAutoDrawable drawable) {
    System.err.println("Gears: Dispose");
    setGears(0, 0, 0);
  }

  @Override
public void display(GLAutoDrawable drawable) {
    // Turn the gears' teeth
    angle += 2.0f;

    // Get the GL corresponding to the drawable we are animating
    GL2 gl = drawable.getGL().getGL2();

    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    // Special handling for the case where the GLJPanel is translucent
    // and wants to be composited with other Java 2D content
    if (GLProfile.isAWTAvailable() &&
        (drawable instanceof com.jogamp.opengl.awt.GLJPanel) &&
        !((com.jogamp.opengl.awt.GLJPanel) drawable).isOpaque() &&
        ((com.jogamp.opengl.awt.GLJPanel) drawable).shouldPreserveColorBufferIfTranslucent()) {
      gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
    } else {
      gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    }

    // Rotate the entire assembly of gears based on how the user
    // dragged the mouse around
    gl.glPushMatrix();
    gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
    gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
    gl.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);

    // Place the first gear and call its display list
    gl.glPushMatrix();
    gl.glTranslatef(-3.0f, -2.0f, 0.0f);
    gl.glRotatef(angle, 0.0f, 0.0f, 1.0f);
    gl.glCallList(gear1);
    gl.glPopMatrix();

    // Place the second gear and call its display list
    gl.glPushMatrix();
    gl.glTranslatef(3.1f, -2.0f, 0.0f);
    gl.glRotatef(-2.0f * angle - 9.0f, 0.0f, 0.0f, 1.0f);
    gl.glCallList(gear2);
    gl.glPopMatrix();

    // Place the third gear and call its display list
    gl.glPushMatrix();
    gl.glTranslatef(-3.1f, 4.2f, 0.0f);
    gl.glRotatef(-2.0f * angle - 25.0f, 0.0f, 0.0f, 1.0f);
    gl.glCallList(gear3);
    gl.glPopMatrix();

    // Remember that every push needs a pop; this one is paired with
    // rotating the entire gear assembly
    gl.glPopMatrix();
  }

  public static void gear(GL2 gl,
                          float inner_radius,
                          float outer_radius,
                          float width,
                          int teeth,
                          float tooth_depth)
  {
    int i;
    float r0, r1, r2;
    float angle, da;
    float u, v, len;

    r0 = inner_radius;
    r1 = outer_radius - tooth_depth / 2.0f;
    r2 = outer_radius + tooth_depth / 2.0f;

    da = 2.0f * (float) Math.PI / teeth / 4.0f;

    gl.glShadeModel(GL2.GL_FLAT);

    gl.glNormal3f(0.0f, 0.0f, 1.0f);

    /* draw front face */
    gl.glBegin(GL2.GL_QUAD_STRIP);
    for (i = 0; i <= teeth; i++)
      {
        angle = i * 2.0f * (float) Math.PI / teeth;
        gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), width * 0.5f);
        gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), width * 0.5f);
        if(i < teeth)
          {
            gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), width * 0.5f);
            gl.glVertex3f(r1 * (float)Math.cos(angle + 3.0f * da), r1 * (float)Math.sin(angle + 3.0f * da), width * 0.5f);
          }
      }
    gl.glEnd();

    /* draw front sides of teeth */
    gl.glBegin(GL2.GL_QUADS);
    for (i = 0; i < teeth; i++)
      {
        angle = i * 2.0f * (float) Math.PI / teeth;
        gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), width * 0.5f);
        gl.glVertex3f(r2 * (float)Math.cos(angle + da), r2 * (float)Math.sin(angle + da), width * 0.5f);
        gl.glVertex3f(r2 * (float)Math.cos(angle + 2.0f * da), r2 * (float)Math.sin(angle + 2.0f * da), width * 0.5f);
        gl.glVertex3f(r1 * (float)Math.cos(angle + 3.0f * da), r1 * (float)Math.sin(angle + 3.0f * da), width * 0.5f);
      }
    gl.glEnd();

    /* draw back face */
    gl.glBegin(GL2.GL_QUAD_STRIP);
    for (i = 0; i <= teeth; i++)
      {
        angle = i * 2.0f * (float) Math.PI / teeth;
        gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), -width * 0.5f);
        gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), -width * 0.5f);
        gl.glVertex3f(r1 * (float)Math.cos(angle + 3 * da), r1 * (float)Math.sin(angle + 3 * da), -width * 0.5f);
        gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), -width * 0.5f);
      }
    gl.glEnd();

    /* draw back sides of teeth */
    gl.glBegin(GL2.GL_QUADS);
    for (i = 0; i < teeth; i++)
      {
        angle = i * 2.0f * (float) Math.PI / teeth;
        gl.glVertex3f(r1 * (float)Math.cos(angle + 3 * da), r1 * (float)Math.sin(angle + 3 * da), -width * 0.5f);
        gl.glVertex3f(r2 * (float)Math.cos(angle + 2 * da), r2 * (float)Math.sin(angle + 2 * da), -width * 0.5f);
        gl.glVertex3f(r2 * (float)Math.cos(angle + da), r2 * (float)Math.sin(angle + da), -width * 0.5f);
        gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), -width * 0.5f);
      }
    gl.glEnd();

    /* draw outward faces of teeth */
    gl.glBegin(GL2.GL_QUAD_STRIP);
    for (i = 0; i < teeth; i++)
      {
        angle = i * 2.0f * (float) Math.PI / teeth;
        gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), width * 0.5f);
        gl.glVertex3f(r1 * (float)Math.cos(angle), r1 * (float)Math.sin(angle), -width * 0.5f);
        u = r2 * (float)Math.cos(angle + da) - r1 * (float)Math.cos(angle);
        v = r2 * (float)Math.sin(angle + da) - r1 * (float)Math.sin(angle);
        len = (float)Math.sqrt(u * u + v * v);
        u /= len;
        v /= len;
        gl.glNormal3f(v, -u, 0.0f);
        gl.glVertex3f(r2 * (float)Math.cos(angle + da), r2 * (float)Math.sin(angle + da), width * 0.5f);
        gl.glVertex3f(r2 * (float)Math.cos(angle + da), r2 * (float)Math.sin(angle + da), -width * 0.5f);
        gl.glNormal3f((float)Math.cos(angle), (float)Math.sin(angle), 0.0f);
        gl.glVertex3f(r2 * (float)Math.cos(angle + 2 * da), r2 * (float)Math.sin(angle + 2 * da), width * 0.5f);
        gl.glVertex3f(r2 * (float)Math.cos(angle + 2 * da), r2 * (float)Math.sin(angle + 2 * da), -width * 0.5f);
        u = r1 * (float)Math.cos(angle + 3 * da) - r2 * (float)Math.cos(angle + 2 * da);
        v = r1 * (float)Math.sin(angle + 3 * da) - r2 * (float)Math.sin(angle + 2 * da);
        gl.glNormal3f(v, -u, 0.0f);
        gl.glVertex3f(r1 * (float)Math.cos(angle + 3 * da), r1 * (float)Math.sin(angle + 3 * da), width * 0.5f);
        gl.glVertex3f(r1 * (float)Math.cos(angle + 3 * da), r1 * (float)Math.sin(angle + 3 * da), -width * 0.5f);
        gl.glNormal3f((float)Math.cos(angle), (float)Math.sin(angle), 0.0f);
      }
    gl.glVertex3f(r1 * (float)Math.cos(0), r1 * (float)Math.sin(0), width * 0.5f);
    gl.glVertex3f(r1 * (float)Math.cos(0), r1 * (float)Math.sin(0), -width * 0.5f);
    gl.glEnd();

    gl.glShadeModel(GL2.GL_SMOOTH);

    /* draw inside radius cylinder */
    gl.glBegin(GL2.GL_QUAD_STRIP);
    for (i = 0; i <= teeth; i++)
      {
        angle = i * 2.0f * (float) Math.PI / teeth;
        gl.glNormal3f(-(float)Math.cos(angle), -(float)Math.sin(angle), 0.0f);
        gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), -width * 0.5f);
        gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), width * 0.5f);
      }
    gl.glEnd();
  }

  class GearsKeyAdapter extends KeyAdapter {
    @Override
	public void keyPressed(KeyEvent e) {
    	System.out.println("KEY!");
        int kc = e.getKeyCode();
        if(KeyEvent.VK_LEFT == kc) {
            view_roty -= 1;
        } else if(KeyEvent.VK_RIGHT == kc) {
            view_roty += 1;
        } else if(KeyEvent.VK_UP == kc) {
            view_rotx -= 1;
        } else if(KeyEvent.VK_DOWN == kc) {
            view_rotx += 1;
        }
    }
  }

  class GearsMouseAdapter extends MouseAdapter {
      @Override
	public void mousePressed(MouseEvent e) {
        prevMouseX = e.getX();
        prevMouseY = e.getY();
      }

      @Override
	public void mouseReleased(MouseEvent e) {
      }

      @Override
	public void mouseDragged(MouseEvent e) {
        final int x = e.getX();
        final int y = e.getY();
        int width=0, height=0;
        Object source = e.getSource();
        if(source instanceof Window) {
            Window window = (Window) source;
            width=window.getSurfaceWidth();
            height=window.getSurfaceHeight();
        } else if(source instanceof GLAutoDrawable) {
        	GLAutoDrawable glad = (GLAutoDrawable) source;
            width=glad.getSurfaceWidth();
            height=glad.getSurfaceHeight();
        } else if (GLProfile.isAWTAvailable() && source instanceof java.awt.Component) {
            java.awt.Component comp = (java.awt.Component) source;
            width=comp.getWidth();
            height=comp.getHeight();
        } else {
            throw new RuntimeException("Event source neither Window nor Component: "+source);
        }
        float thetaY = 360.0f * ( (float)(x-prevMouseX)/(float)width);
        float thetaX = 360.0f * ( (float)(prevMouseY-y)/(float)height);

        prevMouseX = x;
        prevMouseY = y;

        view_rotx += thetaX;
        view_roty += thetaY;
      }
  }
}
