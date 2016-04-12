/*
 * Copyright (c) 2015 SuicSoft / SuiciStudios(tm).
 *
 * Author : Suici Doga (suiciwd@gmail.com , suiciwd@gmail.com)  / contributors
 *
 * Contact : suiciwd@gmail.com , suiciwd@outlook.com , https://gitter.im/SuicSoft/SuicSoft
 *
 * Website : http://suicsoft.com , http://suicsoft.github.io
 *
 * App created and programmed by SuicSoft, designed by SuiciStudios (SuicSoft).
 *
 * License :
 *
 *
 *
 */

package com.suicsoft.software.littlespdfmerge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suici on 12/19/15.
 */
public class ListenerList<L> {
    private List<L> listenerList = new ArrayList<L>();

    public interface FireHandler<L> {
        /**
         * @param listener
         * @throws IOException
         */
        void fireEvent(L listener) throws IOException;
    }

    public void add(L listener) {
        listenerList.add(listener);
    }

    public void fireEvent(FireHandler<L> fireHandler) throws IOException {
        List<L> copy = new ArrayList<>(listenerList);
        for (L l : copy) fireHandler.fireEvent(l);
    }

    public void remove(L listener) {
        listenerList.remove(listener);
    }

    public List<L> getListenerList() {
        return listenerList;
    }
}