package it.mcblock.mcblockit.api.queue;

import java.io.*;
import java.util.PriorityQueue;

import com.google.gson.Gson;

public class Queue extends PriorityQueue<QueueItem> {
    private static final long serialVersionUID = 1L;
    private final String queueFolder = "plugins/MCBlockIt/processingQueue/";
    private final String banQueue = this.queueFolder + "bans";
    private final String unbanQueue = this.queueFolder + "unbans";
    private final Gson gson;

    public Queue() {
        this.gson = new Gson();
        final File file = new File(this.queueFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.load(this.banQueue, BanItem.class);
        this.load(this.unbanQueue, UnbanItem.class);
    }

    @Override
    public boolean add(QueueItem item) {
        if (item == null) {
            return false;
        }
        boolean success;
        synchronized (this) {
            success = super.add(item);
        }
        this.update();
        return success;
    }

    @Override
    public boolean remove(Object item) {
        boolean success;
        synchronized (this) {
            success = super.remove(item);
        }
        this.update();
        return success;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void load(String location, Class clazz) {
        try {
            final File file = new File(location);
            if (file.exists()) {
                final BufferedReader input = new BufferedReader(new FileReader(file));
                String line;
                while ((line = input.readLine()) != null) {
                    if (line.length() < 10) {
                        continue;
                    }
                    QueueItem item = null;
                    item = (QueueItem)this.gson.fromJson(line, clazz);
                    super.add(item);

                }
                input.close();
            }
        } catch (final IOException e) {
            System.out.println("Failed to read " + location);
            e.printStackTrace();
        }
    }

    private void update() {
        try {
            final BufferedWriter outputBans = new BufferedWriter(new FileWriter(this.banQueue));
            final BufferedWriter outputUnbans = new BufferedWriter(new FileWriter(this.unbanQueue));

            synchronized (this) {
                for (final QueueItem item : this) {
                    if (item instanceof BanItem) {
                        outputBans.write(this.gson.toJson(item) + "\n");
                    } else if (item instanceof UnbanItem) {
                        outputUnbans.write(this.gson.toJson(item) + "\n");
                    }
                }
            }
            outputBans.close();
            outputUnbans.close();
        } catch (final IOException e) {
            System.out.println("Failed to write");
            e.printStackTrace();
        }
    }
}