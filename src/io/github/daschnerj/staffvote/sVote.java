package io.github.daschnerj.staffvote;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class sVote extends JavaPlugin implements Listener {
	
	Timer timer = new Timer();

	@Override
	public void onEnable() {
		loadConfiguration();
		load();
		getServer().getPluginManager().registerEvents(this, this);
		this.getCommand("vote").setExecutor(this);
		try {
			scheduleElection();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	// Fired when plugin is disabled
	@Override
	public void onDisable() {
		save();

	}

	public ArrayList<UUID> canidates = new ArrayList<UUID>();

	public HashMap<UUID, UUID> votes = new HashMap<>();

	public HashMap<UUID, String> users = new HashMap<>();

	public ArrayList<UUID> staff = new ArrayList<UUID>();

	public void save() {
		if (!votes.isEmpty())
			saveVotes();
		if (!users.isEmpty())
			saveUsers();
		if (!canidates.isEmpty())
			saveCanidates();
		if (!staff.isEmpty())
			saveStaff();

		this.saveConfig();
	}

	public void load() {
		if (new File(getDataFolder().toString()).exists()) {
			if (this.getConfig().contains("vote.canidates"))
				loadCanidates();
			if (this.getConfig().contains("vote.users"))
				loadUsers();
			if (this.getConfig().contains("vote.votes"))
				loadVotes();
			if (this.getConfig().contains("vote.staff"))
				loadStaff();

			this.getConfig().set("vote.canidates", null);
			this.getConfig().set("vote.users", null);
			this.getConfig().set("vote.votes", null);
			this.getConfig().set("vote.staff", null);
			this.saveConfig();

		} else {
			Bukkit.getServer().getConsoleSender().sendMessage("Folder does not exist!");
		}
	}

	public void saveVotes() {
		ArrayList<String> voteList = new ArrayList<String>();
		for (UUID u : votes.keySet()) {
			voteList.add(u.toString() + "%limit%" + votes.get(u));
		}
		this.getConfig().set("vote.votes", voteList.toArray());
	}

	public void loadVotes() {
		Bukkit.getServer().getConsoleSender().sendMessage("Loading votes!");
		List<String> voteList = this.getConfig().getStringList("vote.votes");
		Bukkit.getServer().getConsoleSender().sendMessage("Votes size is: " + voteList.size());
		for (String s : voteList) {
			Bukkit.getServer().getConsoleSender().sendMessage("Loading vote: " + s);
			String split[] = s.split("%limit%");
			votes.put(UUID.fromString(split[0]), UUID.fromString(split[1]));
		}
	}

	public void saveUsers() {
		ArrayList<String> userList = new ArrayList<String>();
		for (UUID u : users.keySet()) {
			userList.add(u.toString() + "%limit%" + users.get(u));
		}
		this.getConfig().set("vote.users", userList.toArray());
	}

	public void loadUsers() {
		Bukkit.getServer().getConsoleSender().sendMessage("Loading voting users!");
		List<String> userList = this.getConfig().getStringList("vote.users");
		Bukkit.getServer().getConsoleSender().sendMessage("Users size is: " + userList.size());
		for (String s : userList) {
			Bukkit.getServer().getConsoleSender().sendMessage("Loading user: " + s);
			String split[] = s.split("%limit%");
			users.put(UUID.fromString(split[0]), split[1]);
		}
	}

	public void saveCanidates() {
		ArrayList<String> cList = new ArrayList<String>();
		for (UUID u : canidates) {
			cList.add(u.toString());
		}
		this.getConfig().set("vote.canidates", cList.toArray());
	}

	public void saveStaff() {
		ArrayList<String> sList = new ArrayList<String>();
		for (UUID u : staff) {
			sList.add(u.toString());
		}
		this.getConfig().set("vote.staff", sList.toArray());
	}

	public void loadCanidates() {
		Bukkit.getServer().getConsoleSender().sendMessage("Loading voted on canidates!");
		List<String> ids = this.getConfig().getStringList("vote.canidates");
		for (String s : ids) {
			Bukkit.getServer().getConsoleSender().sendMessage("Loading canidate: " + s);
			canidates.add(UUID.fromString(s));
		}
	}

	public void loadStaff() {
		Bukkit.getServer().getConsoleSender().sendMessage("Loading voted on staff!");
		List<String> ids = this.getConfig().getStringList("vote.staff");
		for (String s : ids) {
			Bukkit.getServer().getConsoleSender().sendMessage("Loading staff: " + s);
			staff.add(UUID.fromString(s));
		}
	}

	public UUID getUUID(Player p) {
		if (p != null)
			return p.getUniqueId();
		else
			return null;
	}

	public String getName(UUID u) {
		if (users.containsKey(u))
			return users.get(u);
		else
			return Bukkit.getServer().getOfflinePlayer(u).getName();
	}

	public void updateName(UUID u) {
		users.put(u, getName(u));
	}

	public void vote(UUID u, String n) {
		UUID c = getUUID(Bukkit.getServer().getPlayer(n));
		if (c != null) {
			vote(u, c);
		} else
			Bukkit.getPlayer(u).sendMessage(ChatColor.RED + "That is an invalid player.");
	}

	public void vote(UUID u, UUID c) {
		if (canidates.contains(c)) {
			votes.put(u, c);
			Bukkit.getPlayer(u).sendMessage(ChatColor.AQUA + "You have voted!");
		} else
			Bukkit.getPlayer(u).sendMessage(ChatColor.RED + "This player is not a canidate!");
	}

	public void vote(UUID u, Integer i) {
		if (i < canidates.size() && i > -1)
			vote(u, canidates.get(i));
		else
			Bukkit.getPlayer(u).sendMessage(ChatColor.RED + "You have entered in an invalid index number!");

	}

	public void removeVote(UUID u) {
		if (votes.containsKey(u))
			votes.remove(u);
		Bukkit.getPlayer(u).sendMessage(ChatColor.AQUA + "Your vote has been removed!");
	}

	public void addCanidate(String c) {

		UUID u = getUUID(Bukkit.getPlayer(c));
		if (u != null)
			canidates.add(u);

	}

	public void removeCanidate(String c) {
		UUID u = getUUID(Bukkit.getPlayer(c));
		canidates.remove(u);
	}

	public void clearCanidates() {
		purgeVotes();
		canidates.clear();
	}

	public HashMap<UUID, Integer> tallyVotes() {

		HashMap<UUID, Integer> tally = new HashMap<UUID, Integer>();
		for (UUID u : votes.keySet()) {
			UUID c = votes.get(u);
			if (tally.containsKey(c))
				tally.put(c, tally.get(c) + 1);
			else
				tally.put(c, 1);
		}

		return tally;
	}

	public void displayTally(Player p) {
		HashMap<UUID, Integer> tally = tallyVotes();
		String title = ChatColor.GREEN + "The Current Count for Votes are: ";
		p.sendMessage(title);
		for (UUID u : canidates) {
			if (tally.containsKey(u))
				p.sendMessage(ChatColor.AQUA + getName(u) + " - " + tally.get(u));
			else
				p.sendMessage(ChatColor.RED + getName(u) + " - 0");
		}
	}
	
	public void displayTime(Player p) {
		Long time = getTimeTillElection();
		String title = ChatColor.GREEN + "The current days till next election is: " + ChatColor.GOLD + ((int) (time / (1000*60*60*24)));
		p.sendMessage(title);
		
	}

	public void checkVote(Player p) {
		if (votes.containsKey(p.getUniqueId()))
			p.sendMessage(ChatColor.GREEN + "You have voted for " + ChatColor.AQUA + getName(votes.get(p.getUniqueId()))
					+ ChatColor.GREEN + ".");
		else
			p.sendMessage(ChatColor.RED + "You have yet to vote for anyone.");
	}

	public void listCanidates(Player p) {
		if (!canidates.isEmpty()) {
			p.sendMessage(ChatColor.GREEN + "The list of canidates are:");
			int i = 0;
			for (UUID u : canidates) {
				p.sendMessage(ChatColor.WHITE + String.valueOf(i) + ". " + getName(u));
				i++;
			}
		} else {
			p.sendMessage(ChatColor.RED + "There are no canidates!");
		}
	}

	public boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	public void purgeVotes() {
		votes.clear();
	}

	public void helpVote(Player p) {
		String title = ChatColor.GREEN + "The commands to vote are:";
		String help = ChatColor.AQUA + "/vote help " + ChatColor.GRAY + "- to look at the commands to vote.";
		String list = ChatColor.AQUA + "/vote list " + ChatColor.GRAY + "- to see list of canidates.";
		String vote = ChatColor.AQUA + "/vote [name/index] " + ChatColor.GRAY + "- to cast your vote.";
		String check = ChatColor.AQUA + "/vote check" + ChatColor.GRAY + "- to check who you voted for.";
		String unvote = ChatColor.AQUA + "/vote remove " + ChatColor.GRAY + "- to uncast your vote.";
		String tally = ChatColor.AQUA + "/vote tally" + ChatColor.GRAY + "- to see the current tally of votes.";
		String time = ChatColor.RED + "/vote time " + ChatColor.GRAY + "- to get days before next election.";
		String purge = ChatColor.RED + "/vote purge " + ChatColor.GRAY + "- to purge all votes.";
		String add = ChatColor.RED + "/vote add [name] " + ChatColor.GRAY + "- to add a canidate.";
		String delete = ChatColor.RED + "/vote delete [name] " + ChatColor.GRAY + "- to remove a canidate.";
		String clear = ChatColor.RED + "/vote clear " + ChatColor.GRAY + "- to clear all canidates.";

		p.sendMessage(title);
		p.sendMessage(help);
		p.sendMessage(list);
		p.sendMessage(vote);
		p.sendMessage(check);
		p.sendMessage(unvote);
		p.sendMessage(tally);
		p.sendMessage(time);
		if (p.isOp() || p.hasPermission("play.vote.admin")) {
			p.sendMessage(purge);
			p.sendMessage(add);
			p.sendMessage(delete);
			p.sendMessage(clear);
		}
	}

	public void noPerm(Player p) {
		p.sendMessage(ChatColor.RED + "You do not have permission for voting.");
	}

	public void cmdHelp(Player p) {
		if (p.hasPermission("play.vote.vote"))
			helpVote(p);
		else
			noPerm(p);
	}

	public void cmdList(Player p) {
		if (p.hasPermission("play.vote.vote"))
			listCanidates(p);
		else
			noPerm(p);
	}

	public void cmdTally(Player p) {
		if (p.hasPermission("play.vote.vote"))
			displayTally(p);
		else
			noPerm(p);
	}
	
	public void cmdTime(Player p) {
		if (p.hasPermission("play.vote.vote"))
			displayTime(p);
		else
			noPerm(p);
	}
	

	public void cmdVote(Player p, String u) {
		if (p.hasPermission("play.vote.vote")) {
			if (isInteger(u))
				vote(p.getUniqueId(), Integer.parseInt(u));
			else
				vote(p.getUniqueId(), u);
		} else
			noPerm(p);

	}

	public void cmdCheck(Player p) {
		if (p.hasPermission("play.vote.vote"))
			checkVote(p);
		else
			noPerm(p);
	}

	public void cmdRemove(Player p) {
		if (p.hasPermission("play.vote.vote"))
			removeVote(p.getUniqueId());
		else
			noPerm(p);
	}

	public void cmdPurge(Player p) {
		if (p.hasPermission("play.vote.admin")) {
			purgeVotes();
			p.sendMessage(ChatColor.GREEN + "Votes have been purged.");
		} else
			p.sendMessage(ChatColor.RED + "You are not a voter admin.");
	}

	public void cmdAdd(Player p, String c) {
		if (p.hasPermission("play.vote.admin")) {
			addCanidate(c);
			p.sendMessage(ChatColor.GREEN + "Canidate has been added!");
		} else
			p.sendMessage(ChatColor.RED + "You are not a voter admin.");

	}

	public void cmdDelete(Player p, String c) {
		if (p.hasPermission("play.vote.admin")) {
			removeCanidate(c);
			p.sendMessage(ChatColor.GREEN + "Canidate has been removed.");
		} else
			p.sendMessage(ChatColor.RED + "You are not a voter admin.");
	}

	public void cmdClear(Player p) {
		if (p.hasPermission("play.vote.admin")) {
			clearCanidates();
			p.sendMessage(ChatColor.GREEN + "All canidates have been removed.");
		} else
			p.sendMessage(ChatColor.RED + "You are not a voter admin.");

	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		getLogger().log(Level.INFO, "Player " + event.getPlayer().getName() + " is logging in!");
		Player p = event.getPlayer();
		updateName(p.getUniqueId());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("vote")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (args.length == 0) {
					cmdHelp(p);
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("help"))
						cmdHelp(p);
					else if (args[0].equalsIgnoreCase("list"))
						cmdList(p);
					else if (args[0].equalsIgnoreCase("check"))
						cmdCheck(p);
					else if (args[0].equalsIgnoreCase("remove"))
						cmdRemove(p);
					else if (args[0].equalsIgnoreCase("purge"))
						cmdPurge(p);
					else if (args[0].equalsIgnoreCase("clear"))
						cmdClear(p);
					else if (args[0].equalsIgnoreCase("tally"))
						cmdTally(p);
					else if (args[0].equalsIgnoreCase("time"))
						cmdTime(p);
					else
						cmdVote(p, args[0]);
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("add"))
						cmdAdd(p, args[1]);
					else if (args[0].equalsIgnoreCase("delete"))
						cmdDelete(p, args[1]);
					else
						cmdHelp(p);
				} else {
					cmdHelp(p);
				}
			} else {
				sender.sendMessage("You need to be a player in order to do this command.");
			}
			return true;
		}
		return false;
	}

	public void loadConfiguration() {
		// See "Creating you're defaults"
		this.getConfig().options().copyDefaults(true); // NOTE: You do not have
														// to use "plugin." if
														// the class extends the
														// java plugin
		// Save the config whenever you manipulate it
		this.saveConfig();

	}

	public void makeHelper(UUID u) {
		staff.add(u);
		Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "pex user " + u + " group add Helper");
	}

	public void makeModerator(UUID u) {
		staff.add(u);
		Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "pex user " + u + " group add Moderator");
	}

	public void removeStaff(UUID u) {
		Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "pex user " + u + " group remove Moderator");
		Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "pex user " + u + " group remove Helper");
		staff.remove(u);
	}

	public void clearStaff() {
		for (UUID u : staff) {
			removeStaff(u);
		}
	}

	public ArrayList<Integer> determineStaffCount() {

		ArrayList<Integer> counts = new ArrayList<Integer>();

		Integer ModeratorRatio = 1;
		Integer HelperRatio = 2;
		Integer VoteSet = 20;
		Integer VoteCount = votes.size();

		Integer Helpers = HelperRatio * ((int) Math.ceil(((double) VoteCount) / ((double) VoteSet)));
		Integer Moderators = ModeratorRatio * (VoteCount / VoteSet);

		counts.add(Helpers);
		counts.add(Moderators);

		return counts;

	}

	public ArrayList<UUID> determineTopCanidates() {

		ArrayList<UUID> topCanidates = new ArrayList<UUID>();

		HashMap<UUID, Integer> counts = tallyVotes();
		
		while(!counts.isEmpty())
		{
			UUID topCanidate = getTopCanidate(counts);
			topCanidates.add(topCanidate);
			counts.remove(topCanidate);
		}

		return topCanidates;

	}
	
	public long getTimeTillElection()
	{
		Calendar voteDay = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		voteDay.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		voteDay.set(Calendar.HOUR, 12);
		voteDay.set(Calendar.MINUTE, 0);
		voteDay.set(Calendar.SECOND, 0);
		
		return voteDay.getTimeInMillis() - today.getTimeInMillis();
	}
	
	public void scheduleElection() throws ParseException
	{
		java.util.Timer timer = new java.util.Timer();
		
		timer.schedule(new TimerTask() {

            @Override
            public void run() {
                makeStaff();

            }
        }, getTimeTillElection());
	}
	
	public void makeStaff()
	{
		ArrayList<UUID> topCanidates = determineTopCanidates();
		ArrayList<Integer> staffCount = determineStaffCount();
		
		if(staffCount.get(1) > 0)
		{
			for(int i = 0; i < staffCount.get(1); i++)
			{
				if(!topCanidates.isEmpty())
				{
					makeModerator(topCanidates.get(0));
					topCanidates.remove(0);
				}
			}
		}
		if(staffCount.get(0) > 0)
		{
			for(int i = 0; i < staffCount.get(0); i++)
			{
				if(!topCanidates.isEmpty())
				{
					makeHelper(topCanidates.get(0));
					topCanidates.remove(0);
				}
				
			}
		}
		
		
	}

	public UUID getTopCanidate(HashMap<UUID, Integer> list)
	{
		
		UUID topPicked = null;
		if(!list.isEmpty())
		{
			ArrayList<UUID> topCanidates = new ArrayList<UUID>();
		
			Integer currentMax = 0;
		
			for(UUID u : list.keySet())
			{
				if(topPicked == null)
				{
					topCanidates.add(u);
					currentMax = list.get(u);
				}
				else if(currentMax < list.get(u))
				{
					topCanidates.clear();
					currentMax = list.get(u);
					topCanidates.add(u);
				}
				else if(currentMax == list.get(u))
				{
					topCanidates.add(u);
				}
				else
				{
					
				}
			}
		
			if(topCanidates.size() == 1)
			{
				topPicked = topCanidates.get(0);
			}
			else
			{
				int i = (int)(Math.random() * ((topCanidates.size())));
				topPicked = topCanidates.get(i);
			}
			
		}
		
		return topPicked;
	}

}
