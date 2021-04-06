package it.unibo.cautiousExplorerActor.cautiousActor;

import it.unibo.cautiousExplorerActor.robotWithActor.RobotMovesInfo;
import it.unibo.supports2021.ActorBasicJava;
import it.unibo.supports2021.IssWsHttpJavaSupport;
import org.json.JSONObject;

import javax.swing.plaf.synth.SynthTextAreaUI;

public class CautiousExplorerActor extends ActorBasicJava {
    final String forwardMsg   = "{\"robotmove\":\"moveForward\", \"time\": 350}";
    final String backwardMsg  = "{\"robotmove\":\"moveBackward\", \"time\": 350}";
    final String turnLeftMsg  = "{\"robotmove\":\"turnLeft\", \"time\": 300}";
    final String turnRightMsg = "{\"robotmove\":\"turnRight\", \"time\": 300}";
    final String haltMsg      = "{\"robotmove\":\"alarm\", \"time\": 100}";
    private IssWsHttpJavaSupport support;
    private enum State {Start,Walking,Obstacle,SuperaObstacle,Rotazione}
    private int denDistance = 0;
    private int explorationRadius=0; // inizializzato per il primo giro
    private int rotationDone=0;
    private String movesDone="";
    private State currentState = State.Start;
    private boolean tripStopped=false;
    private boolean lastExplorationGotObstacle=false;
    private boolean inDen=false;
    private int miSonoGiratoAllaFine=0;
    private int miSonoGiratoNellaDen=0;
    private String stringMosseInvertite="";


    public CautiousExplorerActor(String name,IssWsHttpJavaSupport support){
        super(name);
        this.support=support;
    }



    protected void fsm(String move,String endmove) {
        System.out.println( myname + " | fsm state=" + currentState + " tripStopped=" + tripStopped
                + " rotationDone=" + rotationDone + " move=" + move + " endmove=" + endmove);

        switch (currentState){
            case Start:{
                    if(lastExplorationGotObstacle){
                        currentState=State.SuperaObstacle;
                        //this.send("superaOstacolo");
                    }else {
                        denDistance++;
                        explorationRadius++;
                        rotationDone=0;
                        movesDone="";
                        currentState= State.Walking;
                        doStep();
                        movesDone+="w";
                    }
                break;
            }//START
            case Walking:{
                    if(denDistance==explorationRadius){
                        this.send("turning");
                        currentState=State.Rotazione;
                    }
                    else if(rotationDone==4){
                        currentState=State.Start;
                        this.send("startApp");
                    }else if(move.equals("moveForward") && endmove.equals("false")) {
                        currentState = State.Obstacle;
                        stringMosseInvertite= new StringBuilder(movesDone).reverse().toString();
                        //autoinviarsi un messaggio
                        //---------------------------------//
                        this.send("Obstacle");
                    }else if(denDistance<explorationRadius  && endmove.equals("true")){
                            doStep();
                            denDistance++;
                            movesDone+="w";
                        }
                break;
            }//Walking
            case Rotazione:{
                    rotationDone++;
                    denDistance=0;
                    turnLeft();
                    movesDone+="l";
                    currentState=State.Walking;
                    break;
            }//ROTAZIONE
            case Obstacle:{

                if(miSonoGiratoAllaFine<2){
                        turnLeft();
                        miSonoGiratoAllaFine++;
                    }else if(!stringMosseInvertite.isEmpty()) {
                    char mossaDaFare= stringMosseInvertite.charAt(0);///aggiungi tryCatch
                    stringMosseInvertite = stringMosseInvertite.substring(1,stringMosseInvertite.length());
                    if(mossaDaFare=='w'){
                        doStep();
                    }else if(mossaDaFare=='l'){
                        turnRight();
                    }else if(mossaDaFare=='r'){
                        turnLeft();
                    }
                }else  if(miSonoGiratoNellaDen<2){
                    turnLeft();
                    miSonoGiratoNellaDen++;
                }else {
                    //siamo nella tana
                    denDistance=0;
                    miSonoGiratoNellaDen=0;
                    miSonoGiratoAllaFine=0;
                    lastExplorationGotObstacle=true;
                    currentState=State.Start;
                    this.send("startApp");
                }
                break;
            }//OBSTACLE
            case SuperaObstacle:{
                //doSuperaOstacolo
                lastExplorationGotObstacle=false;
                currentState=State.Walking;
                doStep();
            }//SuperaObstacle
        }
    }

    @Override
    protected void handleInput(String msg ) {     //called when a msg is in the queue
        //System.out.println( name + " | input=" + msgJsonStr);
        if(msg.equals("startApp")) fsm("msg","");
        else if(msg.equals("turning")) fsm("finitaLarotation","true");
        else if(msg.equals("Obstacle")) fsm("trovatoOstacolo","true");
        else if(msg.equals("superaOstacolo")) fsm("superiamoOstacolo","true");
        else  msgDriven( new JSONObject(msg) );
    }

    protected void msgDriven( JSONObject infoJson){

        if( infoJson.has("endmove") )        fsm(infoJson.getString("move"), infoJson.getString("endmove"));
    //    else if( infoJson.has("sonarName") ) handleSonar(infoJson);
      //    else if( infoJson.has("collision") ) handleCollision(infoJson);
    }

    protected void handleSonar( JSONObject sonarinfo ){
        String sonarname = (String)  sonarinfo.get("sonarName");
        int distance     = (Integer) sonarinfo.get("distance");
        //System.out.println("RobotApplication | handleSonar:" + sonarname + " distance=" + distance);
    }
    protected void handleCollision( JSONObject collisioninfo ){
        //we should handle a collision  when there are moving obstacles
        //in this case we could have a collision even if the robot does not move
        //String move   = (String) collisioninfo.get("move");
        //System.out.println("RobotApplication | handleCollision move=" + move  );
    }

    //------------------------------------------------
    protected void doStep(){
        support.forward( forwardMsg);
        delay(1000); //to avoid too-rapid movement
    }
    protected void turnLeft(){
        support.forward( turnLeftMsg );
        delay(500); //to avoid too-rapid movement
    }
    protected void turnRight(){
        support.forward( turnRightMsg );
        delay(500); //to avoid too-rapid movement
    }

}
