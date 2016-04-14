package me.aheadlcx.scrolltouch.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.aheadlcx.scrolltouch.R;
import me.aheadlcx.scrolltouch.library.widget.MyScrollViewEx;
import me.aheadlcx.scrolltouch.library.widget.ScrollTouchViewEx;
import me.aheadlcx.scrolltouch.model.Star;
import me.aheadlcx.scrolltouch.utils.Utils;

/**
 * Description:
 * Creator: aheadlcx
 * Date:16/4/14 下午2:24
 */
public class ItemDetailFrag extends BaseFragment {

    private ViewPager viewPager;
    private MyScrollViewEx scrollView;
    private ScrollTouchViewEx scrollTouchView;
    private TextView txtTitle;
    private Star mStar;
    public static final String star = "star";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_home_detail_scroll, null);
        initData();
        initViews(view);
        initScrollTouchView();
        showHalf();
        return view;
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mStar = bundle.getParcelable(star);
            if (mStar == null) {
                throw new IllegalArgumentException("find not star");
            }
        }
    }

    private void initScrollTouchView() {
        int screenHeight = Utils.getScreenHeight(getContext());
        int allOpen = 0;
        int halfOpen = 1000;
        scrollTouchView.setExitOffSet(screenHeight);
        scrollTouchView.setAllOpen(allOpen);
        scrollTouchView.setHalfOpen(halfOpen);

    }

    private void initViews(View view) {
        this.scrollTouchView = (ScrollTouchViewEx) view.findViewById(R.id.scrollTouchView);
        this.scrollView = (MyScrollViewEx) view.findViewById(R.id.scrollView);
        this.viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        txtTitle = (TextView) view.findViewById(R.id.txtTitle);

        txtTitle.setText(mStar.getName());

        scrollTouchView.setScrollViewEx(scrollView);

        MyPagerAdapter adapter = new MyPagerAdapter(getViews());
        viewPager.setAdapter(adapter);
    }

    public void showHalf() {
        scrollTouchView.showHaflStatus();
    }

    public List<View> getViews() {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setBackgroundResource(mStar.getAvatarResid());
            final int j = i;
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = "imageView onClick == " + j;
                    Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();

                }
            });
            views.add(imageView);
        }

        return views;
    }

    public class MyPagerAdapter extends PagerAdapter {
        private List<View> views;

        public MyPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }
    }
}
