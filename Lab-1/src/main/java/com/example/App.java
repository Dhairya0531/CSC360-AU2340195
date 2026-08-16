package com.example;

import javax.swing.JFrame;

public class App extends JFrame {
    public App() {
        super("Bordered Square");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);
        add(new SquarePanel());
    }
}
