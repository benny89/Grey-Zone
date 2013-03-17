package greyzone.creature;


import greyzone.trigger.Trigger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import jade.core.Actor;
import jade.fov.RayCaster;
import jade.fov.ViewField;
import jade.ui.Camera;
import jade.ui.TermPanel;
import jade.ui.Terminal;
import jade.util.datatype.ColoredChar;
import jade.util.datatype.Coordinate;
import jade.util.datatype.Direction;


public class Player extends Creature implements Camera
{
    private TermPanel term;
    private ViewField fov;

    private int stepCount;

    public Player(TermPanel term)
    {
        super(ColoredChar.create('@'));
        this.term = term;
        fov = new RayCaster();
        stepCount=10; // after stepamount many steps hp gets reduced by 1
        setXp(0);
        setHp(30); // hp at beginning of game
    }
    
    ////////////////////////////////////////////////////////////////
    //////////// get set methods  
    ////////////////////////////////////////////////////////////////
    public Terminal getTerm()
    {
    	return term;	
    }
    public void setTerm(TermPanel term)
    {
    	this.term = term;
    }

    
    ////////////////////////////////////////////////////////////////
    //////////// Methods that were already implemented
    ////////////////////////////////////////////////////////////////
    @Override
    public void act()
    
    {       	
        try
        {
            char key;
            key = term.getKey();
            switch(key)
            {
                case 'q':
                    expire();
                    break;
                case 'H':
                {
                	if(term.getMenu("Inv")==false)
                		term.setMenu("Inv",true);
                	else
                		term.setMenu("Inv",false);
                }          
                case '1':
                {
                	if(term.getMenu("seeAll")==false)
                		term.setMenu("seeAll",true);
                	else
                		term.setMenu("seeAll",false);
                }    
                default:
                    Direction dir = Direction.keyToDir(key);
                    if(dir != null)
                    {
                    	move(dir);
                    	

                    // HP reducing takes place here:..........................................................................
                    	addStep();
                 
                    	if (getSteps() % stepamount == 0)
                    	{
                    		setHp(getHp() - 1);
              
                    	}
                    	if (getHp()==0) expire();
                    //........................................................................................................
                    }
                    	break;	
                    	
            }
            
            contact();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        
        fight();
    }
    
    public void contact() {
    	
     	Trigger trigger =  getWorld().getActorAt(Trigger.class, pos());
    	String messages;
     	
		if (trigger != null) {
    		messages = trigger.retrieveMessages().toString();
    		System.out.println(messages);
    		expire();
		}
    }
    @Override
    public Collection<Coordinate> getViewField()
    {
        return fov.getViewField(world(), pos(), 5);
    }

	public int getStepamount() {
		return stepCount;
	}

	public void setStepamount(int stepCount) {
		this.stepCount = stepCount;
	}
	
	/*
	 * contact made:
	 * contactMade():
	 * uses the trigger and finds out if the player is at the same place with
	 * any other actors. If yes, which actor?
	 * Use a switch to determine and act accordingly.
	 * 
	 * 
	 */
	
	public void fight(){
		
		Collection<Monster> DraculasGang = getWorld().getActorsAt(greyzone.creature.Monster.class, pos());
		if (DraculasGang != null){
			for(Monster gangMember : DraculasGang){
				attack(gangMember);
			}
		}
	}
}
