package org.tapchain.realworld;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public MenuFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MenuFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MenuFragment newInstance(String param1, String param2) {
        MenuFragment fragment = new MenuFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_menu, container, false);
        ImageButton finish = (ImageButton)v.findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).finish();
            }
        });
        ImageButton dustbox = (ImageButton)v.findViewById(R.id.dustbox);
        dustbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).getEditor().reset();
            }
        });
        final ImageButton menuToggle = (ImageButton)v.findViewById(R.id.menuToggle);
        menuToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        boolean gridshow = false;
                        GridFragment f = ((MainActivity)getActivity()).getGrid();
                        if (f != null) {
                            gridshow = f.toggle();
                        }
                        if (gridshow)
                            menuToggle.setImageResource(R.drawable.pulldown);
                        else
                            menuToggle.setImageResource(R.drawable.pullup);
            }
        });
        return v;
    }

    //        addButton(view_bottom_left, R.drawable.dust, true,
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        getEditor().reset();
//                    }
//                });
//        addButton(view_bottom_left, R.drawable.pullup, true,
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        boolean gridshow = false;
//                        GridFragment f = getGrid();
//                        if (f != null) {
//                            gridshow = f.toggle();
//                        }
//                        if (gridshow)
//                            ((ImageView) v)
//                                    .setImageResource(R.drawable.pulldown);
//                        else
//                            ((ImageView) v).setImageResource(R.drawable.pullup);
//                    }
//                });
//        addButton(view_bottom_left, R.drawable.config, true,
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        try {
//                            Log.w("JSON TEST", getEditor().editActor().getChain().toJSON().toString());
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });

}
