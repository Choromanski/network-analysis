
public class Edge implements Comparable<Edge> { 

    private final int v;
    private final int w;
    private final String type;
    private final int speed;
    private final double length;
    private final double latency;
    private final int COPPER = 230000000;
    private final int OPTICAL = 200000000;

    public Edge(int v, int w, String type, int speed, double length) {
        if (v < 0) throw new IllegalArgumentException("vertex index must be a nonnegative integer");
        if (w < 0) throw new IllegalArgumentException("vertex index must be a nonnegative integer");
        if (!type.equals("optical") && !type.equals("copper")) throw new IllegalArgumentException("Type is not optical or copper");
        if (speed < 0) throw new IllegalArgumentException("Speed must be a nonnegative integer");
        if (length < 0 )throw new IllegalArgumentException("length must be a nonnegative integer");
        this.v = v;
        this.w = w;
        this.type = type;
        this.speed = speed;
        this.length = length;
        if(type.equals("optical")){
            latency =  (this.length/OPTICAL);
        }else{
            latency = (this.length/COPPER);
        }
    }



    public double length() {
        return length;
    }

    public String type() {
        return type;
    }

    public double latency(){
        return latency;
    }

    public int speed() {
        return speed;
    }

    public int either() {
        return v;
    }

    public int other(int vertex) {
        if      (vertex == v) return w;
        else if (vertex == w) return v;
        else throw new IllegalArgumentException("Illegal endpoint");
    }

    @Override
    public int compareTo(Edge that) {
        return Double.compare(this.speed, that.speed);
    }

    public String toString() {
        return String.format("%d-%d %s %d %f %f", v, w, type, speed, length, latency);
    }

}

