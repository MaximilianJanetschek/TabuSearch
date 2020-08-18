import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

class draw
{
    public static void  drawRoutes(Solution s, String fileName) {

        int VRP_Y = 1600;
        int VRP_INFO = 400;
        int X_GAP = 1200;
        int margin = 60;
        int marginNode = 2;


        int XXX = VRP_INFO + X_GAP;
        int YYY = VRP_Y;

        BufferedImage output = new BufferedImage(XXX, YYY, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, XXX, YYY);
        g.setColor(Color.BLACK);


        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        
        double bestSolutionValue = -1 * Double.MAX_VALUE;
        int bestSolution = -1;
        
        for (int i = 0; i<s.bestSolutionFeasible.size(); i++) {
        	if (s.bestSolutionFeasible.get(i).tourProfit > bestSolutionValue) {
        		bestSolutionValue = s.bestSolutionFeasible.get(i).tourProfit;
        		bestSolution = i;
        	}
        }
        

        for (int k = 0; k < s.bestSolutionFeasible.get(bestSolution).vehiclesTour.length ; k++)	 {
            for (int i = 0; i < s.bestSolutionFeasible.get(bestSolution).vehiclesTour[k].Route.size(); i++)	 {
                Node n = s.bestSolutionFeasible.get(bestSolution).vehiclesTour[k].Route.get(i);
                if (n.NODE_X > maxX) maxX = n.NODE_X;
                if (n.NODE_X < minX) minX = n.NODE_X;
                if (n.NODE_Y > maxY) maxY = n.NODE_Y;
                if (n.NODE_Y < minY) minY = n.NODE_Y;
            }
        }

        int mX = XXX - 2 * margin;
        int mY = VRP_Y - 2 * margin;

        int A, B;
        if ((maxX - minX) > (maxY - minY))	{
            A = mX;
            B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
            if (B > mY)	{
                B = mY;
                A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
            }
        }
        else	{
            B = mY;
            A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
            if (A > mX)	{
                A = mX;
                B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
            }
        }

        // Draw Route
        for (int i = 0; i < s.bestSolutionFeasible.get(bestSolution).vehiclesTour.length -1; i++)	{     
        	if (i <= 2) {
        		g.setColor(Color.BLUE);
        		if (i <= 1) {
        			g.setColor(Color.RED);
        			if (i ==0) {
        				g.setColor(Color.GREEN);
        			}
        		}
        	}
        	else {
        		g.setColor(Color.BLACK);
        	}
            String index = "Tour" + (i+1);
            g.drawString(index, 40, (i*20+60));
            for (int j = 1; j < s.bestSolutionFeasible.get(bestSolution).vehiclesTour[i].Route.size() ; j++) {
                Node n;
                n = s.bestSolutionFeasible.get(bestSolution).vehiclesTour[i].Route.get(j-1);

                int ii1 = (int) ((double) (A) * ((n.NODE_X - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                int jj1 = (int) ((double) (B) * (0.5 - (n.NODE_Y - minY) / (maxY - minY)) + (double) mY / 2) + margin;

                n = s.bestSolutionFeasible.get(bestSolution).vehiclesTour[i].Route.get(j);
                int ii2 = (int) ((double) (A) * ((n.NODE_X - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                int jj2 = (int) ((double) (B) * (0.5 - (n.NODE_Y - minY) / (maxY - minY)) + (double) mY / 2) + margin;


                g.drawLine(ii1, jj1, ii2, jj2);
            }
        }
        g.setColor(Color.black);
        for (int i = 0; i < s.bestSolutionFeasible.get(bestSolution).vehiclesTour.length ; i++)	 {           
        	for (int j = 0; j < s.bestSolutionFeasible.get(bestSolution).vehiclesTour[i].Route.size() ; j++) {
                Node n = s.bestSolutionFeasible.get(bestSolution).vehiclesTour[i].Route.get(j);

                int ii = (int) ((double) (A) * ((n.NODE_X  - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                int jj = (int) ((double) (B) * (0.5 - (n.NODE_Y - minY) / (maxY - minY)) + (double) mY / 2) + margin;
                if (i != 0) {
                    g.fillOval(ii - 3 * marginNode, jj - 3 * marginNode, 6 * marginNode, 6 * marginNode); //2244
                    String id = Integer.toString(n.NodeId);
                    g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode); //88
                } 
                else {
                    g.fillRect(ii - 3 * marginNode, jj - 3 * marginNode, 6 * marginNode, 6 * marginNode);  //4488
                    String id = Integer.toString(n.NodeId);
                    g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode); //88
                }
            }
        }

        String cst = "VRP solution for "+ s.instance.NUMBER_OF_CUSTOMERS + " customers with Profit: " + s.bestSolutionFeasible.get(bestSolution).tourProfit; //still to be updated
        g.drawString(cst, 40, 40);; 

        fileName = fileName + ".png";
        File f = new File(fileName);
        try	{
            ImageIO.write(output, "PNG", f);
        } 
        catch (IOException ex) {
            //  Logger.getLogger(s.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
