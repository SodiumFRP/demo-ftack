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
  private final float view_rotx = -45f, view_roty = 0f;
  private final float view_rotz = 45f;
  private int block=0;

  private StreamSink<Unit> sClick = new StreamSink<>();
  private Cell<Scene> scene;

  public static void main(String[] args) {
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

    final Ftack ftack = new Ftack();
    canvas.addGLEventListener(ftack);

    frame.add(canvas, java.awt.BorderLayout.CENTER);
    frame.validate();

    frame.setVisible(true);
    animator.start();
  }

  public Ftack() {
    SecondsTimerSystem sys = new SecondsTimerSystem();
    scene = Transaction.run(() -> {
	    Match match = new Match(sys, sClick);
	    return match.scene;
    });
  }

  @Override
public void init(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();

    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    System.err.println("INIT GL IS: " + gl.getClass().getName());
    System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
    System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
    System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));

    float pos[] = { -1.0f, 1.0f, 10.0f, 0.0f };

    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
    gl.glEnable(GL2.GL_CULL_FACE);
    gl.glEnable(GL2.GL_LIGHTING);
    gl.glEnable(GL2.GL_LIGHT0);
    gl.glEnable(GL2.GL_DEPTH_TEST);

    if(0>=block) {
        block = gl.glGenLists(1);
        gl.glNewList(block, GL2.GL_COMPILE);
        mkBlock(gl);
        gl.glEndList();
    }

    gl.glEnable(GL2.GL_NORMALIZE);

    MouseListener mouse = new FtackMouseAdapter();
    KeyListener keys = new FtackKeyAdapter();

    if (drawable instanceof Window) {
        Window window = (Window) drawable;
        window.addMouseListener(mouse);
        window.addKeyListener(keys);
    } else if (GLProfile.isAWTAvailable() && drawable instanceof java.awt.Component) {
        java.awt.Component comp = (java.awt.Component) drawable;
        new AWTMouseAdapter(mouse, drawable).addTo(comp);
        new AWTKeyAdapter(keys, drawable).addTo(comp);
    }
  }

  @Override
public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    System.err.println("Gears: Reshape "+x+"/"+y+" "+width+"x"+height);
    GL2 gl = drawable.getGL().getGL2();

    gl.setSwapInterval(1);

    float h = (float)height / (float)width;

    gl.glMatrixMode(GL2.GL_PROJECTION);

    gl.glLoadIdentity();
    gl.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 60.0f);
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadIdentity();
    gl.glTranslatef(0.0f, 0.0f, -40.0f);
    float sz = 0.65f;
    gl.glScalef(sz, sz, sz);
  }

  @Override
public void dispose(GLAutoDrawable drawable) {
    System.err.println("Gears: Dispose");
    block = 0;
  }

  @Override
public void display(GLAutoDrawable drawable) {
    Scene sc = scene.sample();

    // Get the GL corresponding to the drawable we are animating
    GL2 gl = drawable.getGL().getGL2();

    gl.glClearColor(0.6f, 0.6f, 0.8f, 1.0f);

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

    gl.glPushMatrix();
    gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
    gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
    gl.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);
    gl.glScalef(sc.zoom, sc.zoom, sc.zoom);
    gl.glTranslated(0, 0, -sc.zCentre);

    // Draw the blocks from the scene
    for (Block bl : sc.blocks) {
	    gl.glPushMatrix();
	    float col[] = { bl.colour.r, bl.colour.g, bl.colour.b, 1f };
	    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, col, 0);
	    Point centre = bl.centre();
	    gl.glTranslated(centre.y, centre.x, centre.z);
        gl.glScalef(bl.depth(), bl.width(), bl.height());
	    gl.glCallList(block);
	    gl.glPopMatrix();
    }

    gl.glPopMatrix();
  }

  public static void mkBlock(GL2 gl)
  {
    gl.glShadeModel(GL2.GL_SMOOTH);
    gl.glBegin(GL2.GL_QUADS);
    gl.glNormal3f(0f, 0f, 1f);
    float h = 0.5f;
    gl.glVertex3f(h,-h,h);
    gl.glVertex3f(h,h,h);
    gl.glVertex3f(-h,h,h);
    gl.glVertex3f(-h,-h,h);

    gl.glNormal3f(-h, 0f, 0f);
    gl.glVertex3f(-h,-h,-h);
    gl.glVertex3f(-h,-h,h);
    gl.glVertex3f(-h,h,h);
    gl.glVertex3f(-h,h,-h);

    gl.glNormal3f(0f, -h, 0f);
    gl.glVertex3f(h,-h,-h);
    gl.glVertex3f(h,-h,h);
    gl.glVertex3f(-h,-h,h);
    gl.glVertex3f(-h,-h,-h);
    gl.glEnd();
  }

  class FtackKeyAdapter extends KeyAdapter {
    @Override
	public void keyPressed(KeyEvent e) {
    	Ftack.this.sClick.send(Unit.UNIT);
    }
  }

  class FtackMouseAdapter extends MouseAdapter {
      @Override
	public void mousePressed(MouseEvent e) {
    	Ftack.this.sClick.send(Unit.UNIT);
      }

      @Override
	public void mouseReleased(MouseEvent e) {
      }

      @Override
	public void mouseDragged(MouseEvent e) {
      }
  }
}
