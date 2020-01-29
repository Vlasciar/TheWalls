import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
public class MyWorld extends World
{
    boolean twoD = true;    
    public MyWorld()
    {            
        super(70*13, 850, 1);//70-FOV 13-px/angle       
        prepare();
        if(!twoD){inviz2d();}
    }    
    int length = 13;//  px/angle
    public void act()
    {        
        setBackground("bg.png");
        getBackground().setColor(Color.BLACK);            
        getBackground().fillRect(0, 0,getWidth(),getHeight()/2+15);
        getBackground().setColor(Color.GRAY);
        getBackground().fillRect(0, getWidth()/2-15,getWidth(),getHeight()/2);

        MouseInfo mouse = Greenfoot.getMouseInfo();
        if(Greenfoot.mousePressed(null)==false)
        {
            rays_draw();
            for(int i=0;i<k;i++)
            { 
                render_wall(i);
            }
        }       
    }    
    int FOV=35;//half of the fov    
    Ray[] rays = new Ray[361];//max fov 360
    Wall[] walls = new Wall[255]; 
    Main_Ray main_Ray = new Main_Ray();  
    Player pl = new Player(main_Ray);    
    int k=0;
    private void prepare()
    {
        addObject(pl,50,50);
        rays_init(); 
        prep_image();
        prep_edge_walls();
        prep_walls();
    }    

    public static BufferedImage cropImage(BufferedImage bufferedImage, int x, int y, int width, int height)
    {
        BufferedImage croppedImage = bufferedImage.getSubimage(x, y, width, height);
        return croppedImage;
    }

    GreenfootImage img = new GreenfootImage("wall_1.png");
    public void prep_image()
    {
        int imgWidth = img.getWidth();///(getWidth()/k+1);//15
        File original = new File("images/wall_1.png");        
        File[] cuts = new File[imgWidth];       
        try {
            BufferedImage image = ImageIO.read(original);            
            for(int i=0;i<imgWidth;i++)
            {
                cuts[i] = new File("images/wall_1_seg/slice"+i+".png");
                BufferedImage sec = cropImage(image,i,0,1,img.getHeight());
                ImageIO.write(sec, "png",cuts[i]);
            }  
        }
        catch(IOException e) {
            e.printStackTrace();                
        }
    }
    int next_slice;
    int scl;     
    GreenfootImage fillwall;
    //int gap;
    private void render_wall(int i)
    {                 
        int angle = main_Ray.getRotation()-rays[i].getRotation();
        double cos = Math.cos(Math.toRadians(angle));
        int record = (int)(cos*rays[i].record); 
        double diagonal = getWidth() / Math.cos(Math.toRadians(45));
        double height = ((double)getHeight()/diagonal /record)*70000;
        if(height>getHeight()*10) height=getHeight()*10;
        if(height<1)height=1;
        int color = rays[i].color;
        getBackground().setColor(Color.BLACK);            
        getBackground().fillRect(i * length, 0,length,getHeight()/2+15);
        getBackground().setColor(Color.GRAY);
        getBackground().fillRect(i * length, getWidth()/2-15,length,getHeight()/2);

        int slice = (int)((rays[i].procent_hit*(img.getWidth()-1)))%64;//0-img.Width  
        rays[i].slice = slice;
        int next_slice;
        int x;
        int gap = walls[rays[i].record_index].gap;
        if(i<k-1)// && rays[i].record_index==rays[i+1].record_index)
        {                
            next_slice = (int)((rays[i+1].procent_hit*(img.getWidth()-1)))%64;//0-img.Width                
            walls[rays[i].record_index].gap=Math.abs(rays[i].slice-next_slice)+1;      
            if(gap>13)gap=13;
            if(rays[i].slice<next_slice)
            {                   
                for(int z=0;z<gap;z++)
                {
                    if(rays[i].slice+z<img.getWidth())
                    {    
                        GreenfootImage fillwall=new GreenfootImage("wall_1_seg/slice"+(rays[i].slice+z)+".png");                
                        fillwall.scale((13/gap+1), (int)height);                          
                        getBackground().drawImage(fillwall,i * length+z*(13/gap+1),getHeight()/2-(int)height/2);
                    }
                }
            }
            else if(rays[i].slice>next_slice)  
            {
                for(int z=gap-1;z>=0;z--)
                {
                    if(rays[i].slice-gap<0)gap=rays[i].slice;
                    if(rays[i].slice-z>0 )
                    {                         
                        GreenfootImage fillwall=new GreenfootImage("wall_1_seg/slice"+(rays[i].slice-z)+".png");                
                        fillwall.scale((13/gap+1), (int)height);                          
                        getBackground().drawImage(fillwall,i * length+z*(13/gap+1),getHeight()/2-(int)height/2);
                    }
                }
            }
            else if(rays[i].slice==next_slice) 
            {
                GreenfootImage fillwall=new GreenfootImage("wall_1_seg/slice"+(rays[i].slice)+".png");                
                fillwall.scale((13), (int)height);
                getBackground().drawImage(fillwall,i * length,getHeight()/2-(int)height/2);
            }
        }
    }

    private void rays_init()
    {       
        addObject(main_Ray,pl.getX(),pl.getY()); 
        k=0;
        for(int i=main_Ray.getRotation()-FOV;i<=main_Ray.getRotation()+FOV;i++,k++)
        {            
            rays[k] = new Ray();
            addObject(rays[k],pl.getX(),pl.getY());            
            //showText(String.valueOf(k), 600, 300);
        }        
    } 

    private void rays_draw()////act////
    {
        k=0;
        for(int i=main_Ray.getRotation()-FOV;i<=main_Ray.getRotation()+FOV;i++,k++)
        { 
            rays[k].setLocation(pl.getX(),pl.getY());
            rays[k].setRotation(main_Ray.getRotation()-FOV+k);
            rays[k].drawn=false;
        }
    }

    public void prep_walls()
    {
        int w=4;               
        for(int i=0;i<7;i++)
        {
            walls[w] = new Wall(1,w);
            walls[w].setRotation(90);
            addObject(walls[w],200,50+100*i);
            w++;
        }
        for(int i=0;i<5;i++)
        {
            walls[w] = new Wall(1,w);
            walls[w].setRotation(0);
            addObject(walls[w],250+100*i,700);
            w++;
        }
        walls[w] = new Wall(1,w);
        walls[w].setRotation(0);
        walls[w].setImage("Wall2.png");
        addObject(walls[w],725,700);
        w++;
        for(int i=0;i<5;i++)
        {
            walls[w] = new Wall(1,w);
            walls[w].setRotation(90);
            addObject(walls[w],750,650-100*i);
            w++;
        }
        walls[w] = new Wall(1,w);
        walls[w].setRotation(90);
        walls[w].setImage("Wall2.png");
        addObject(walls[w],750,175);
        w++;
        for(int i=0;i<3;i++)
        {
            walls[w] = new Wall(1,w);
            walls[w].setRotation(0);
            addObject(walls[w],700-100*i,150);
            w++;
        }
        walls[w] = new Wall(1,w);
        walls[w].setRotation(0);
        walls[w].setImage("Wall2.png");
        addObject(walls[w],425,150);
        w++;
        for(int i=0;i<4;i++)
        {
            walls[w] = new Wall(1,w);
            walls[w].setRotation(90);
            addObject(walls[w],400,200+100*i);
            w++;
        }
        for(int i=0;i<2;i++)
        {
            walls[w] = new Wall(1,w);
            walls[w].setRotation(0);
            addObject(walls[w],450+100*i,550);
            w++;
        }
        for(int i=0;i<2;i++)
        {
            walls[w] = new Wall(1,w);
            walls[w].setRotation(90);
            addObject(walls[w],600,500-100*i);
            w++;
        }
        walls[w] = new Wall(1,w);
        walls[w].setRotation(90);
        walls[w].setImage("Wall2.png");
        addObject(walls[w],600,325);
        w++;
        walls[w] = new Wall(1,w);
        walls[w].setRotation(0);
        walls[w].setImage("Wall2.png");
        addObject(walls[w],575,300);
        w++;
        for(int i=0;i<2;i++)
        {
            walls[w] = new Wall(1,w);
            walls[w].setRotation(90);
            addObject(walls[w],550,500-100*i);
            w++;
        }
        walls[w] = new Wall(1,w);
        walls[w].setRotation(90);
        walls[w].setImage("Wall2.png");
        walls[w].solid=false;
        addObject(walls[w],550,325);

        w++;
    }

    public void prep_edge_walls()
    {
        int w=0; 
        int W=getWidth();
        int H=getHeight();
        walls[w] = new Wall(0,w);
        walls[w].setRotation(0);
        walls[w].edgeWall=true;
        addObject(walls[w],0,0); 
        walls[w].x1=0;walls[w].y1=0;walls[w].x2=W;walls[w].y2=0;
        w++;        
        walls[w] = new Wall(0,w);
        walls[w].setRotation(0);
        walls[w].edgeWall=true;
        addObject(walls[w],0,0);
        walls[w].x1=0;walls[w].y1=H;walls[w].x2=W;walls[w].y2=H;
        w++;
        walls[w] = new Wall(0,w);
        walls[w].setRotation(90);
        walls[w].edgeWall=true;
        addObject(walls[w],0,0); 
        walls[w].x1=0;walls[w].y1=0;walls[w].x2=0;walls[w].y2=H;
        w++;
        walls[w] = new Wall(0,w);
        walls[w].setRotation(90);
        walls[w].edgeWall=true;
        addObject(walls[w],0,0);
        walls[w].x1=W;walls[w].y1=0;walls[w].x2=W;walls[w].y2=H;

    }

    public void inviz2d()
    {
        for(int i=0;i<walls.length;i++){
            if(walls[i]!=null)
                walls[i].getImage().setTransparency(0);
        }
        main_Ray.getImage().setTransparency(0);
        pl.getImage().setTransparency(0);
        for(int i=0;i<rays.length;i++){
            if(rays[i]!=null)
                rays[i].getImage().setTransparency(0);
        }
    }
}
