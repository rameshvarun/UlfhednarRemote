package net.varunramesh.ulfhednarremote;

/**
 * Created by Varun on 6/10/2015.
 */
public final class RemoteState {
    private int id = 0;
    private float x = 0;
    private float y = 0;
    private boolean attack = false;
    private boolean special = false;

    public synchronized void setId(int id) { this.id = id; }
    public synchronized int getId() { return id; }

    public synchronized void setX(float x) { this.x = x; }
    public synchronized float getX() { return x; }

    public synchronized void setY(float y) { this.y = y; }
    public synchronized float getY() { return y; }

    public synchronized void setAttack(boolean attack) { this.attack = attack; }
    public synchronized boolean getAttack() { return attack; }

    public synchronized void setSpecial(boolean special) { this.special = special; }
    public synchronized boolean getSpecial() { return special; }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(":").append(x).append(":").append(y).append(":").append(attack).append(":").append(special);
        return sb.toString();
    }
}
