
package nz.sodium.ftack;

import nz.sodium.*;
import nz.sodium.time.*;
import javaslang.collection.Array;

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
  private float view_rotx = -45f, view_roty = 0f;
  private final float view_rotz = 45f;
  private int block=0;
  private final int swapInterval;

  private int prevMouseX, prevMouseY;

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

    float pos[] = { -1.0f, 1.0f, 10.0f, 0.0f };

    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
    gl.glEnable(GL2.GL_CULL_FACE);
    gl.glEnable(GL2.GL_LIGHTING);
    gl.glEnable(GL2.GL_LIGHT0);
    gl.glEnable(GL2.GL_DEPTH_TEST);

    /* make the gears */
    if(0>=block) {
        block = gl.glGenLists(1);
        gl.glNewList(block, GL2.GL_COMPILE);
        mkBlock(gl);
        gl.glEndList();
        System.err.println("gear1 list created: "+block);
    } else {
        System.err.println("gear1 list reused: "+block);
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

	int levels = 10;
	Array<Block> blocks = Array.empty();
	for (int i = 0; i < levels; i++) {
		float w = (float)((levels - i) * 5f / (float)levels);
		float z0 = (float)i;
		float z1 = (float)i+1;
		blocks = blocks.append(new Block(new Point(-w,-w,z0), new Point(w,w,z1),
				               Colour.fromHSV((float)i * 6,0.4f,1.0f)));
	}
	Scene sc = new Scene(blocks, (float)levels * 0.5f, 1f);

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

    // Rotate the entire assembly of gears based on how the user
    // dragged the mouse around
    gl.glPushMatrix();
    gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
    gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
    gl.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);
    gl.glScalef(sc.zoom, sc.zoom, sc.zoom);

    gl.glTranslated(0, 0, -sc.zCentre);
    // Place the first gear and call its display list
    for (Block bl : sc.blocks) {
	    gl.glPushMatrix();
	    float col[] = { bl.colour.r, bl.colour.g, bl.colour.b, 1f };
	    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, col, 0);
        gl.glScalef(bl.width(), bl.depth(), bl.height());
	    Point centre = bl.centre();
	    gl.glTranslated(centre.x, centre.y, centre.z);
	    gl.glCallList(block);
	    gl.glPopMatrix();
    }

    // Remember that every push needs a pop; this one is paired with
    // rotating the entire gear assembly
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
