package org.jboss.seam.examples.princessrescue.ftest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Functional test for the PrincessRescue example
 *
 * @author Ondrej Skutka
 */
@RunWith(Arquillian.class)
public class PrincessRescueTest {
    private static final String MSG_INTRO = "The princess has been kidnaped by a dragon, and you are the only one who can save her. Armed only with your trusty bow you bravely head into the dragon's lair, who know what horrors you will encounter, some say the caves are even home to the dreaded Wumpus.";
    private static final String MSG_ENTRANCE = "You enter the dungeon, with you bow in your hand and your heart in your mouth.";

    private static final String MSG_NEAR_BATS = "You hear a screeching noise.";
    private static final String MSG_BATS = "A swarm of bats lands on you and tries to pick you up. They fail miserably. You swat them away with your bow.";

    private static final String MSG_NEAR_DWARF = "You hear drunken singing.";
    private static final String MSG_DWARF = "You find a drunken dwarven miner. He belches in your direction, falls over, then seems to forget you are there.";
    private static final String MSG_SHOT_DWARF = "You hear a 'Thud', followed by a surprised yell.";
    private static final String MSG_DEAD_DWARF = "You find a dead dwarven miner with something that looks suspiciously like one of your arrows sticking out of his chest. Probably best you don't mention this to anyone...";

    private static final String MSG_NEAR_PIT = "You feel a breeze.";
    private static final String MSG_PIT = "You fall into a bottomless pit. Game Over.";

    private static final String MSG_NEAR_PRINCESS = "You hear a sobbing noise.";
    private static final String MSG_PRINCESS = "You find the princess and quickly free her, and then escape from the dungeon. You both live happily ever after.";

    private static final String MSG_NEAR_DRAGON = "You hear a snoring noise. With every snore you see a flickering light, as if something were breathing flames from its nostrils.";
    private static final String MSG_SHOT_DRAGON = "Your arrow wakes up the dragon, without appearing to do any real damage. The last moments of your life are spent running from an angry dragon.";

    private static final String MAIN_PAGE = "/home.jsf";
    
    @FindBy(id = "bv:next")
    private WebElement NEW_GAME_BUTTON;
    
    public static final String ARCHIVE_NAME = "config-princess-rescue.war";
    public static final String BUILD_DIRECTORY = "target";

    protected enum Direction {
        NORTH, SOUTH, WEST, EAST
    }

    protected enum Action {
        MOVE, SHOT
    }
    
    @ArquillianResource
    URL contextPath;
    
    @Drone
    WebDriver selenium;
    
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(ZipImporter.class, ARCHIVE_NAME).importFrom(new File(BUILD_DIRECTORY + '/' + ARCHIVE_NAME))
                .as(WebArchive.class);
    }

    @Before
    public void startNewGame() throws MalformedURLException {
        selenium.navigate().to(contextPath.toString() + MAIN_PAGE);
        ensureTextPresent(MSG_INTRO);
        
        Graphene.guardHttp(NEW_GAME_BUTTON).click();
        ensureTextPresent(MSG_ENTRANCE);
    }

    /**
     * Start the game, kill the dwarf and rescue the princess
     */
    @Test
    public void findPrincess() {
    	
    	clickAndWait(Action.MOVE, Direction.NORTH);
    	
        ensureTextPresent(MSG_NEAR_DWARF);
        ensureTextPresent(MSG_NEAR_PIT);

        // Take a look at the dwarf
        moveTo(Direction.WEST);
        ensureTextPresent(MSG_DWARF);
        for (Action action : Action.values()) {
            assertTrue(isEditable(action, Direction.EAST));
            assertFalse(isEditable(action, Direction.WEST));
            assertFalse(isEditable(action, Direction.NORTH));
            assertFalse(isEditable(action, Direction.SOUTH));
        }

        // We can still hear the dwarf singing
        moveTo(Direction.EAST);
        ensureTextPresent(MSG_NEAR_DWARF);
        ensureTextPresent(MSG_NEAR_PIT);

        // Kill the drunkard
        shootTo(Direction.WEST);
        ensureTextPresent(MSG_SHOT_DWARF);

        // Bury the evidence
        moveTo(Direction.WEST);
        ensureTextPresent(MSG_DEAD_DWARF);

        // No more bad singer
        moveTo(Direction.EAST);
        assertFalse("Expected the dwarf to be dead already.", isTextPresent(MSG_NEAR_DWARF));
        ensureTextPresent(MSG_NEAR_PIT);

        // Now for the princess!
        moveTo(Direction.NORTH);
        ensureTextPresent(MSG_NEAR_BATS);
        ensureTextPresent(MSG_NEAR_PIT);

        moveTo(Direction.EAST);
        ensureTextPresent(MSG_BATS);
        ensureTextPresent(MSG_NEAR_PIT);

        moveTo(Direction.EAST);
        ensureTextPresent(MSG_NEAR_BATS);

        moveTo(Direction.EAST);
        ensureTextPresent(MSG_NEAR_DRAGON);

        moveTo(Direction.NORTH);
        ensureTextPresent(MSG_NEAR_PRINCESS);

        // Happy end
        moveTo(Direction.EAST);
        ensureTextPresent(MSG_PRINCESS);
        ensureButtonsDisabled();
    }

    /**
     * Start the game, tickle the dragon and die
     */
    @Test
    public void dieHeroically() {
    	moveTo(Direction.NORTH);
        ensureTextPresent(MSG_NEAR_DWARF);
        ensureTextPresent(MSG_NEAR_PIT);

        moveTo(Direction.NORTH);
        ensureTextPresent(MSG_NEAR_BATS);
        ensureTextPresent(MSG_NEAR_PIT);

        moveTo(Direction.EAST);
        ensureTextPresent(MSG_BATS);
        ensureTextPresent(MSG_NEAR_PIT);

        moveTo(Direction.EAST);
        ensureTextPresent(MSG_NEAR_BATS);

        moveTo(Direction.EAST);
        ensureTextPresent(MSG_NEAR_DRAGON);

        shootTo(Direction.EAST);
        ensureTextPresent(MSG_SHOT_DRAGON);
        ensureButtonsDisabled();
    }

    /**
     * Start the game, enjoy a free fall.
     */
    @Test
    public void dieImpressively() {
        moveTo(Direction.NORTH);
        ensureTextPresent(MSG_NEAR_DWARF);
        ensureTextPresent(MSG_NEAR_PIT);

        moveTo(Direction.EAST);
        ensureTextPresent(MSG_PIT);
        ensureButtonsDisabled();
    }

    /**
     * Start the game, kill the dwarf, start over, ensure the dwarf is alive again (and drunken).
     */
    @Test
    public void quitEarly() {
    	moveTo(Direction.NORTH);
        ensureTextPresent(MSG_NEAR_DWARF);
        ensureTextPresent(MSG_NEAR_PIT);

        shootTo(Direction.WEST);
        ensureTextPresent(MSG_SHOT_DWARF);

        Graphene.guardHttp(NEW_GAME_BUTTON).click();
        ensureTextPresent(MSG_INTRO);
        Graphene.guardHttp(NEW_GAME_BUTTON).click();
        ensureTextPresent(MSG_ENTRANCE);

        moveTo(Direction.NORTH);
        ensureTextPresent(MSG_NEAR_DWARF);
        ensureTextPresent(MSG_NEAR_PIT);
    }

    /**
     * Ensures that all the move and shot buttons present on the page are disabled. Fails if not.
     */
    private void ensureButtonsDisabled() {
        for (Direction direction : Direction.values()) {
            for (Action action : Action.values()) {
                assertFalse(selenium.findElement(getLocator(action, direction)).isEnabled());
            }
        }
    }
    
    private boolean isTextPresent(String text) {
    	return selenium.findElement(By.tagName("body")).getText().contains(text);
    }

    /**
     * Ensures that the specified text is present on the page. Fails if not.
     */
    private void ensureTextPresent(String text) {
        assertTrue("Expected the following text to be present: \"" + text + "\"", isTextPresent(text));
    }
    
    private boolean isEditable(Action action, Direction direction) {
    	return selenium.findElement(getLocator(action, direction)).isEnabled();
    }

    /**
     * Returns the move or shot button specified by parameters.
     */
    private By getLocator(Action action, Direction direction) {
    	return By.id("bv:" + action.toString().toLowerCase() + direction.toString().toLowerCase());
    }

    private void clickAndWait(Action action, Direction direction) {
    	Graphene.guardHttp(selenium.findElement(getLocator(action, direction))).click();
    }
    
    private void moveTo(Direction direction) {
    	clickAndWait(Action.MOVE, direction);
    }
    
    private void shootTo(Direction direction) {
    	clickAndWait(Action.SHOT, direction);
    }
}
