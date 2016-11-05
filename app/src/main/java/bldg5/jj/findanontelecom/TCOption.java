package bldg5.jj.findanontelecom;

public class TCOption
{
    private int ID;
    private double dblLat;
    private double dblLong;
    // 0 = inactive, 1 = active
    private int active;

    // getters
    public int getID()
    {
        return this.ID;
    }

    public double getLat()
    {
        return this.dblLat;
    }

    public double getLong()
    {
        return this.dblLong;
    }

    public int getActive()
    {
        return this.active;
    }

    // setters
    public void setID(int i)
    {
        this.ID = i;
    }

    public void setLat(double dbl)
    {
        this.dblLat = dbl;
    }

    public void setLong(double dbl)
    {
        this.dblLong = dbl;
    }

    public void setActive(int i)
    {
        // valid values are 1 and 0
        if (i != 0 && i != 1)
        {
            throw new IllegalArgumentException("Invalid integer value: " + String.valueOf(i));
        }

        this.active = i;
    }

    public String toString()
    {
        return "{ 'ID' : " + this.ID + ", " +
                " 'Lat' : " + String.valueOf(this.dblLat) + ", " +
                " 'Long' : " + String.valueOf(this.dblLong) + ", " +
                " 'Active' : " + String.valueOf(this.active) + "}";
    }
}
