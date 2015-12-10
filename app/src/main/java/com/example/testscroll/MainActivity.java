package com.example.testscroll;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.testscroll.model.MyPage;
import com.example.testscroll.utils.CharsetDetector;
import com.example.testscroll.view.FlipperLayout;
import com.example.testscroll.view.FlipperLayout.TouchListener;
import com.example.testscroll.view.ReadView;
import com.example.testscrollactivity.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends Activity implements OnClickListener, TouchListener {

    private String text = "";

    private int textLength = 8000;

    private static final int COUNT = 1000;

    private int currentTopEndIndex = 0;
    private int currentShowEndIndex = 0;
    private int currentBottomEndIndex = 0;

    ArrayList<MyPage> pages = new ArrayList<>();

    private static final int MSG_DRAW_TEXT = 1;

    //ReadView curReadView;

    CharBuffer buffer = CharBuffer.allocate(8000);

    //int position = 0;

    //ReadView nextReadView;

    //ReadView preReadView;

    int pageIndex = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DRAW_TEXT:

                    FlipperLayout rootLayout = (FlipperLayout) findViewById(R.id.container);

                    View recoverView = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_new, null);
                    View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_new, null);
                    View view2 = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_new, null);

                    rootLayout.initFlipperViews(MainActivity.this, view2, view1, recoverView);

                    //textLength = text.length();

                    final ReadView readView1 = (ReadView) view1.findViewById(R.id.textview);
                    final ReadView readView2 = (ReadView) view2.findViewById(R.id.textview);

//					if (textLength > COUNT) {
//						textView.setText(text.subSequence(0, COUNT));
//
//						textView = (ReadView) view2.findViewById(R.id.textview);
//						if (textLength > (COUNT << 1)) {
//
//							textView.setText(text.subSequence(COUNT, COUNT << 1));
//
//							currentShowEndIndex = COUNT;
//							currentBottomEndIndex = COUNT << 1;
//						} else {
//							textView.setText(text.subSequence(COUNT, textLength));
//							currentShowEndIndex = textLength;
//							currentBottomEndIndex = textLength;
//						}
//					} else {
//						textView.setText(text.subSequence(0, textLength));
//						currentShowEndIndex = textLength;
//						currentBottomEndIndex = textLength;
//					}

                    buffer.position(0);
                    readView1.setText(buffer);
                    readView1.setLayoutListener(new ReadView.LayoutListener() {
                        @Override
                        public void onLayout(int charNum) {

                            if (pages.size() == 0) {
                                pages.add(new MyPage(pageIndex, charNum));

                                currentShowEndIndex = charNum;
                                buffer.position(currentShowEndIndex);
                                readView2.setText(buffer);
                                readView2.setLayoutListener(new ReadView.LayoutListener() {
                                    @Override
                                    public void onLayout(int charNum) {
                                        if (pages.size() == 1) {
                                            currentBottomEndIndex = charNum;
                                            pages.add(new MyPage(pageIndex + 1, charNum));
                                            pageIndex ++;
                                        }
                                    }
                                });
                            }

                        }
                    });


//                    ViewTreeObserver vto1 = readView1.getViewTreeObserver();
//                    vto1.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                        @Override
//                        public void onGlobalLayout() {
//                            if (currentShowEndIndex > 0) {
//                                return;
//                            }
//
//                            currentShowEndIndex = readView1.getCharNum();
//
//                            position = readView1.getCharNum();
//                            buffer.position(position);
//
//                            readView2.setText(buffer);
//                            nextReadView = readView2;
//
//                            ViewTreeObserver vto2 = readView2.getViewTreeObserver();
//                            vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                                @Override
//                                public void onGlobalLayout() {
//                                    if (currentBottomEndIndex > 0) {
//                                        return;
//                                    }
//                                    currentBottomEndIndex = readView2.getCharNum();
//                                }
//                            });
//                        }
//                    });

//					currentShowEndIndex = textLength;
//					currentBottomEndIndex = textLength;

                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new ReadingThread().start();
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public View createView(final int direction, View curView) {
        //String txt;

        //ReadView curReadView = (ReadView) curView.findViewById(R.id.textview);

        View newView = null;
        if (direction == TouchListener.MOVE_TO_LEFT) { //下一页

            pageIndex++;

            currentTopEndIndex = currentShowEndIndex;
            currentShowEndIndex = currentBottomEndIndex;

            newView = LayoutInflater.from(this).inflate(R.layout.view_new, null);
            ReadView readView = (ReadView) newView.findViewById(R.id.textview);

            buffer.position(currentBottomEndIndex);
            readView.setText(buffer);


            readView.setLayoutListener(new ReadView.LayoutListener() {
                @Override
                public void onLayout(int charNum) {
                    if (pages.size() == pageIndex) {
                        pages.add(new MyPage(pageIndex, charNum));
                        currentBottomEndIndex = currentBottomEndIndex + charNum;

                        Log.d("test003", "currentTopEndIndex=" + currentTopEndIndex +
                                ", currentShowEndIndex=" + currentShowEndIndex
                                + ", currentBottomEndIndex= " + currentBottomEndIndex);
                    }
                }
            });


        } else {  //上一页

            pageIndex --;

            Log.d("test003", "pageIndex=" + pageIndex);

            for(int i = 0; i < pages.size() ; i ++) {
                Log.d("test003", pages.get(i).toString());
            }

            currentBottomEndIndex = currentShowEndIndex;
            currentShowEndIndex = currentTopEndIndex;
            currentTopEndIndex = currentTopEndIndex - pages.get(pageIndex - 1).getPageSize();

            Log.d("test003", "currentTopEndIndex=" + currentTopEndIndex +
                    ", currentShowEndIndex=" + currentShowEndIndex
                    + ", currentBottomEndIndex= " + currentBottomEndIndex);

            newView = LayoutInflater.from(this).inflate(R.layout.view_new, null);
            ReadView readView = (ReadView) newView.findViewById(R.id.textview);

            buffer.position(currentTopEndIndex);
            readView.setText(buffer);

        }

        return newView;
    }

    @Override
    public boolean whetherHasPreviousPage() {
        //return currentShowEndIndex > COUNT;
        return currentTopEndIndex > 0;
    }

    @Override
    public boolean whetherHasNextPage() {
        //return currentShowEndIndex < textLength;
        return true;
    }

    @Override
    public boolean currentIsFirstPage() {
//		boolean should = currentTopEndIndex > COUNT;
//		if (!should) {
//			currentBottomEndIndex = currentShowEndIndex;
//			currentShowEndIndex = currentTopEndIndex;
//			currentTopEndIndex = currentTopEndIndex - COUNT;
//		}

        boolean should = true;

        return should;
    }

    @Override
    public boolean currentIsLastPage() {
//		boolean should = currentBottomEndIndex < textLength;
//		if (!should) {
//			currentTopEndIndex = currentShowEndIndex;
//			final int nextIndex = currentBottomEndIndex + COUNT;
//			currentShowEndIndex = currentBottomEndIndex;
//			if (textLength > nextIndex) {
//				currentBottomEndIndex = nextIndex;
//			} else {
//				currentBottomEndIndex = textLength;
//			}
//		}

        boolean should = true;

        return should;
    }

    private class ReadingThread extends Thread {
        public void run() {
//			AssetManager am = getAssets();
//
//            InputStream inputStream = null;
//            ByteArrayOutputStream outputStream = null;
//
//			try {
//                inputStream = am.open("text.txt");
//				if (inputStream != null) {
//
//                    outputStream = new ByteArrayOutputStream();
//					int i;
//					while ((i = inputStream.read()) != -1) {
//                        outputStream.write(i);
//					}
//					text = new String(outputStream.toByteArray(), "UTF-8");
//
//					mHandler.obtainMessage(MSG_DRAW_TEXT).sendToTarget();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//
//                if (inputStream != null) {
//                    try {
//                        inputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if (outputStream != null) {
//                    try {
//                        outputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }

            BufferedReader reader;
            AssetManager assets = getAssets();
            try {
                InputStream in = assets.open("text.txt");
                Charset charset = CharsetDetector.detect(in);
                reader = new BufferedReader(new InputStreamReader(in, charset));

                reader.read(buffer);

                mHandler.obtainMessage(MSG_DRAW_TEXT).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
