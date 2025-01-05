import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {

    // Inner class representing a playing card
    private class Card {
        String value; // Value of the card (e.g., "A", "2", ..., "K")
        String type;  // Type of the card (e.g., "Hearts", "Diamonds")

        // Constructor to initialize a card with a value and type
        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        // Returns a string representation of the card
        public String toString() {
            return value + "-" + type;
        }

        // Returns the numeric value of the card
        public int getValue() {
            if ("AJQK".contains(value)) { // Check for face cards
                if (value.equals("A")) { // Ace is worth 11 points by default
                    return 11;
                }
                return 10; // Face cards are worth 10 points
            }
            return Integer.parseInt(value); // Numeric cards (2-10)
        }

        // Checks if the card is an Ace
        public boolean isAce() {
            return value.equals("A");
        }

        // Returns the file path to the card's image
        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }

    // Deck of cards
    ArrayList<Card> deck;
    Random random = new Random(); // Random generator for shuffling the deck

    // Dealer's variables
    Card hiddenCard; // Hidden card of the dealer
    ArrayList<Card> dealerHand; // Cards in the dealer's hand
    int dealerSum; // Total value of the dealer's hand
    int dealerAceCount; // Count of Aces in the dealer's hand

    // Player's variables
    ArrayList<Card> playerHand; // Cards in the player's hand
    int playerSum; // Total value of the player's hand
    int playerAceCount; // Count of Aces in the player's hand

    // Game window dimensions
    int boardWidth = 600;
    int boardHeight = boardWidth;

    // Card dimensions
    int cardWidth = 110;
    int cardHeight = 154;

    // GUI components
    JFrame frame = new JFrame("SUPER BLACKJACK");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
                // Draw the dealer's hidden card
                Image hiddenCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!stayButton.isEnabled()) { // Reveal the card if the player has stayed
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage(hiddenCardImg, 20, 20, cardWidth, cardHeight, null);

                // Draw the dealer's visible cards
                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5) * i, 20, cardWidth, cardHeight, null);
                }

                // Draw the player's cards
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, 20 + (cardWidth + 5) * i, 320, cardWidth, cardHeight, null);
                }

                // Display the game result if the player has stayed
                if (!stayButton.isEnabled()) {
                    dealerSum = reduceDealerAce(); // Adjust dealer's sum for Aces
                    playerSum = reducePlayerAce(); // Adjust player's sum for Aces

                    String message = "";
                    if (playerSum > 21) {
                        message = "You Lose.";
                    } else if (dealerSum > 21) {
                        message = "You Win!";
                    } else if (playerSum == dealerSum) {
                        message = "Tie.";
                    } else if (playerSum > dealerSum) {
                        message = "You Win!";
                    } else if (playerSum < dealerSum) {
                        message = "You Lose.";
                    }

                    // Display the result message
                    g.setFont(new Font("Arial", Font.PLAIN, 30));
                    g.setColor(Color.white);
                    g.drawString(message, 220, 250);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel buttonPanel = new JPanel(); // Panel for the control buttons
    JButton hitButton = new JButton("Hit"); // Button for "Hit" action
    JButton stayButton = new JButton("Stay"); // Button for "Stay" action

    // Constructor to initialize the game
    BlackJack() {
        startGame(); // Set up the initial state of the game

        // Style the buttons
        styleButton(hitButton, new Color(28, 162, 86), Color.WHITE);
        styleButton(stayButton, new Color(28, 162, 86), Color.WHITE);

        // Configure the game window
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Configure the game panel
        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(52, 101, 77));
        frame.add(gamePanel);

        // Add buttons to the button panel
        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listener for the "Hit" button
        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Card card = deck.remove(deck.size() - 1); // Draw a card
                playerSum += card.getValue(); // Update player's sum
                playerAceCount += card.isAce() ? 1 : 0; // Update Ace count
                playerHand.add(card); // Add the card to the player's hand

                if (reducePlayerAce() > 21) { // Check if player busts
                    hitButton.setEnabled(false); // Disable the "Hit" button
                }
                gamePanel.repaint(); // Refresh the game panel
            }
        });

        // Add action listener for the "Stay" button
        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hitButton.setEnabled(false); // Disable "Hit" button
                stayButton.setEnabled(false); // Disable "Stay" button

                // Dealer's turn: draw cards until the dealer's sum is >= 17
                while (dealerSum < 17) {
                    Card card = deck.remove(deck.size() - 1); // Draw a card
                    dealerSum += card.getValue(); // Update dealer's sum
                    dealerAceCount += card.isAce() ? 1 : 0; // Update Ace count
                    dealerHand.add(card); // Add the card to the dealer's hand
                }
                gamePanel.repaint(); // Refresh the game panel
            }
        });

        gamePanel.repaint(); // Initial repaint to display the game
    }

    private void styleButton(JButton button, Color backgroundColor, Color textColor) {
        // Set the background and text color of the button
        button.setBackground(backgroundColor);
        button.setForeground(textColor);
        button.setFocusPainted(false); // Remove focus outline
        button.setFont(new Font("Tahoma", Font.BOLD, 12)); // Set font style and size
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25)); // Add padding to the button
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Change cursor to hand when hovering
    
        // Add hover effects to the button
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker()); // Darken the background on hover
            }
    
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor); // Revert to original background when not hovering
            }
    
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.brighter()); // Brighten the background when pressed
            }
    
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker()); // Darken the background when released
            }
        });
    }
    
    public void startGame() {
        // Build and shuffle the deck
        buildDeck();
        shuffleDeck();
    
        // Initialize dealer's hand
        dealerHand = new ArrayList<>();
        dealerSum = 0;
        dealerAceCount = 0;
    
        // Deal a hidden card to the dealer
        hiddenCard = deck.remove(deck.size() - 1); // Remove the last card in the deck
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;
    
        // Deal another visible card to the dealer
        Card card = deck.remove(deck.size() - 1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);
    
        // Debugging output for dealer's initial state
        System.out.println("DEALER:");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerSum);
        System.out.println(dealerAceCount);
    
        // Initialize player's hand
        playerHand = new ArrayList<>();
        playerSum = 0;
        playerAceCount = 0;
    
        // Deal two cards to the player
        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size() - 1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }
    
        // Debugging output for player's initial state
        System.out.println("Player: ");
        System.out.println(playerHand);
        System.out.println(playerSum);
        System.out.println(playerAceCount);
    }
    
    public void buildDeck() {
        // Initialize the deck as an empty list
        deck = new ArrayList<>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"}; // Card values
        String[] type = {"C", "D", "H", "S"}; // Card suits: Clubs, Diamonds, Hearts, Spades
    
        // Create cards for all combinations of values and suits
        for (int i = 0; i < type.length; i++) {
            for (int j = 0; j < values.length; j++) {
                Card card = new Card(values[j], type[i]);
                deck.add(card);
            }
        }
    
        // Debugging output for the built deck
        System.out.println("Build Deck:");
        System.out.println(deck);
    }
    
    public void shuffleDeck() {
        // Shuffle the deck by swapping cards at random positions
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size()); // Generate a random index
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }
    
        // Debugging output for the shuffled deck
        System.out.println("After Shuffle:");
        System.out.println(deck);
    }
    
    public int reducePlayerAce() {
        // Adjust the player's total score by reducing Ace values from 11 to 1 if necessary
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10; // Reduce the value of an Ace from 11 to 1
            playerAceCount--; // Decrease the count of Aces counted as 11
        }
        return playerSum; // Return the adjusted player's total score
    }
    
    public int reduceDealerAce() {
        // Adjust the dealer's total score by reducing Ace values from 11 to 1 if necessary
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10; // Reduce the value of an Ace from 11 to 1
            dealerAceCount--; // Decrease the count of Aces counted as 11
        }
        return dealerSum; // Return the adjusted dealer's total score
    }
}    
