package com.perezjquim.noallerg.util;


public class CyclicThread extends Thread implements CyclicRunnable
{
    private static final int DEFAULT_SAMPLING = 500;
    private int sampling;
    private boolean enabled = false;

    public CyclicThread(int sampling)
    {
        this.sampling = sampling;
    }
    public CyclicThread()
    {
        this.sampling = DEFAULT_SAMPLING;
    }
    public void run()
    {
        enabled = true;
        while(enabled)
        {
            try
            {
                Thread.sleep(sampling);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            iteration();
        }
    }
    public void iteration() {}

    public void kill()
    {
        enabled = false;
    }
    public void restart()
    {
        kill();
        start();
    }
}
