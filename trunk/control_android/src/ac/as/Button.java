package ac.as;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

 

public class Button {

    int pX, pY, Width, Height;
    Paint paint;
    
    Button(int pX, int pY, int Width, int Height) {
        this.pX = pX;
        this.pY = pY;
        this.Width = Width;
        this.Height = Height;
    }

    void Draw(Canvas canvas) {
        paint = new Paint();
        paint.setColor(Color.DKGRAY);
        canvas.drawRect(pX-3, pY-3, pX+Width+3, pY+Height+3, paint);
        paint.setColor(Color.WHITE);
        canvas.drawRect(pX, pY, pX+Width, pY+Height, paint);
    }

    
    boolean IsClicked(float InputX, float InputY) {
        if(InputX >= pX && InputX <= pX+Width && InputY >= pY && InputY <= pY+Height) {
            return true;

        }
        else return false;

    }

}

