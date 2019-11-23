package cz.cvut.fit.miadp.mvcgame.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import cz.cvut.fit.miadp.mvcgame.abstractFactory.GameObjsFac_A;
import cz.cvut.fit.miadp.mvcgame.abstractFactory.IGameObjsFac;
import cz.cvut.fit.miadp.mvcgame.command.AbsCommand;
import cz.cvut.fit.miadp.mvcgame.model.gameobjects.AbsCannon;
import cz.cvut.fit.miadp.mvcgame.model.gameobjects.AbsCollision;
import cz.cvut.fit.miadp.mvcgame.model.gameobjects.AbsEnemy;
import cz.cvut.fit.miadp.mvcgame.model.gameobjects.AbsMissile;
import cz.cvut.fit.miadp.mvcgame.model.gameobjects.AbsModelInfo;
import cz.cvut.fit.miadp.mvcgame.observer.IObservable;
import cz.cvut.fit.miadp.mvcgame.observer.IObserver;

public class GameModel implements IGameModel, IObservable {
    private List<IObserver> myObs;

    private Queue<AbsCommand> unexecutedCmds = new LinkedBlockingQueue<AbsCommand>();
    private Stack<AbsCommand> executedCmds = new Stack<AbsCommand>();

    private IGameObjsFac goFact = new GameObjsFac_A();

    private int score;
    private AbsCannon cannon;
    private AbsModelInfo gameInfo;
    private List<AbsEnemy> enemies;
    private List<AbsMissile> missiles;
    private List<AbsCollision> collisions;

    public GameModel() {
        this.score = 0;

        this.myObs = new ArrayList<IObserver>();

        this.cannon = this.goFact.createCannon();
        this.gameInfo = this.goFact.createModelInfo();

        this.enemies = new ArrayList<AbsEnemy>();
        this.missiles = new ArrayList<AbsMissile>();
        this.collisions = new ArrayList<AbsCollision>();
    }

    public void moveCannonUp() {
        this.cannon.moveUp();

        this.notifyMyObs();
    }

    public void moveCannonDown() {
        this.cannon.moveDown();

        this.notifyMyObs();
    }

    @Override
    public void registerObserver(IObserver obs) {
        this.myObs.add(obs);
    }

    @Override
    public void unregisterObserver(IObserver obs) {
        this.myObs.remove(obs);
    }

    @Override
    public void notifyMyObs() {
        for (IObserver obs : this.myObs) {
            obs.update();
        }
    }

    public List<GameObject> getGameObjects() {
        List<GameObject> go = new ArrayList<GameObject>();

        go.addAll(this.missiles);
        go.addAll(this.enemies);
        go.addAll(this.collisions);
        go.add(this.cannon);
        go.add(this.gameInfo);

        return go;
    }

    public void timeTick() {

        this.executeCmds();

        this.moveMissiles();

        // this.destroyInvisibleGO();
    }

    private void executeCmds() {
        while(!this.unexecutedCmds.isEmpty())
        {
            AbsCommand cmd = this.unexecutedCmds.poll();
            cmd.doExecute();

            this.executedCmds.push(cmd);
        }

        
    }

    public void undoLastCmd()
    {
        if(this.executedCmds.isEmpty()) return;

        AbsCommand cmd = this.executedCmds.pop();
        cmd.unexecute();

        this.notifyMyObs();
    }

    public void moveMissiles() {
        for (AbsMissile m : this.missiles) {
            m.move();
        }

        this.notifyMyObs();
    }

    public void cannonShoot() {
        this.missiles.addAll(this.cannon.shoot());

        this.notifyMyObs();
    }

    public void toggleShootingMode() {
        this.cannon.toggleShootingMode();

        this.notifyMyObs();
    }

    private class Memento {
        private int score;
    }

    public Object createMemento() {
        Memento m = new Memento();
        m.score = this.score;
        // store positions of Missiles,...

        return m;
    }

    public void setMemento(Object memento) {
        Memento m = (Memento) memento;
        this.score = m.score;
        // restore positions of Missiles,...

    }

    @Override
    public void registerCommand(AbsCommand cmd) {
        this.unexecutedCmds.add(cmd);

    }



}
