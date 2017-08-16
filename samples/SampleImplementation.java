package samples;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.bensoft.acis.core.ACIS;
import de.bensoft.acis.core.Action;
import de.bensoft.acis.core.Action.ActionMethod;
import de.bensoft.acis.core.ActionMalformedException;
import de.bensoft.acis.core.ActionPackage;
import de.bensoft.acis.core.ActionResult;
import de.bensoft.acis.core.ActionResult.ActionResultCode;
import de.bensoft.acis.core.ContextConstructorAction;
import de.bensoft.acis.core.ContextDependentAction;
import de.bensoft.acis.core.ContextDestructorAction;
import de.bensoft.acis.core.ContextVisibility;
import de.bensoft.acis.core.Parameter;
import de.bensoft.acis.core.environment.Environment;
import de.bensoft.acis.core.environment.SystemEnvironment;
import de.bensoft.acis.core.environment.SystemProperties;
import de.bensoft.acis.core.environment.UserInfo;
import de.bensoft.acis.core.environment.VisualOutput;
import de.bensoft.acis.core.language.Language;
import de.bensoft.acis.core.language.Sentence;
import de.bensoft.acis.core.language.Word;
import de.bensoft.acis.languages.Unified;
import de.bensoft.acis.server.Server;
import de.bensoft.acis.server.ServerContext;
import de.bensoft.acis.server.User;
import de.bensoft.acis.server.contexts.SampleRequestHandler;
import de.bensoft.acis.utils.ActionPackageFromJarLoader;

public class SampleImplementation {

	public static void main(String[] args) {
		try {
			// create the ACIS system with a SystemEnvironment
			ACIS mySystem = new ACIS(new Unified(), new SystemEnvironment() {

				@Override
				public SystemProperties getSystemProperties() {
					return new SystemProperties("sample.acis", 1, "100817", new Date("08/10/2017"),
							"Ben-Noah Engelhaupt (bensoft.de)", "Sample ACIS system", System.currentTimeMillis());
				}

				@Override
				public UserInfo getUserInfo() {
					return new UserInfo("Mister", "Example", new String[] { "Samply" }, 18, "email@example.com",
							"Sample street 1, 11111 Sampletown, Samplecountry");
				}

				@Override
				public boolean canSpeak() {
					return false;
				}

				@Override
				public void addOutput(String output) {
					addWrittenOutput(output);
					addSpokenOutput(output);
				}

				@Override
				public void addWrittenOutput(String output) {
					System.out.println(output);
				}

				@Override
				public void addSpokenOutput(String output) {

				}

				@Override
				public boolean canRequestInput() {
					return false;
				}

				@Override
				public String requestInput(String message) throws UnsupportedOperationException {
					throw new UnsupportedOperationException("No input requesting supported.");
				}

				@Override
				public boolean hasVisualOutput() {
					return false;
				}

				@Override
				public VisualOutput getVisualOutput() throws UnsupportedOperationException {
					throw new UnsupportedOperationException("No visual output supported.");
				}

			});

			// create a new ActionPackage
			ActionPackage sampleActionPackage = new ActionPackage() {

				@Override
				public String getName() {
					return "com.example.acis.actionpackages.sample";
				}

				@Override
				public String getDescription() {
					return "A sample ActionPackage.";
				}

				@Override
				public Locale getLocale() {
					return Locale.ENGLISH;
				}

				@Override
				public int getMinimumRequiredLibraryVersion() {
					return 170810;
				}

				@Override
				public Action[] getActions(final Language language) throws ActionMalformedException {
					List<Action> actionList = new ArrayList<>(0);

					// simple Action
					actionList.add(new Action("SampleAction", this, ContextVisibility.PUBLIC, "my sample trigger",
							new ActionMethod() {

								@Override
								public ActionResult run(Environment environment, Sentence sentence,
										Parameter[] parameter) {
									environment.addOutput("The sample action was executed.");
									return new ActionResult(ActionResultCode.SUCCESS);
								}

							}));

					// Action with multiple triggers
					actionList.addAll(
							Arrays.asList(Action.getActions("SampleActionArray", this, ContextVisibility.PRIVATE,
									new String[] { "another action with <<e$0>>", "this <<e$0>> is a parameter" },
									new ActionMethod() {

										@Override
										public ActionResult run(Environment environment, Sentence sentence,
												Parameter[] parameter) {
											if (parameter.length > 0) {
												environment.addOutput("The parameter was " + parameter[0].getValue());
												return new ActionResult(ActionResultCode.SUCCESS);
											} else {
												environment.addOutput("Could not find the parameter.");
												if (environment.canRequestInput()) {
													String input = environment.requestInput("What is the parameter?");
													if (input == null)
														return new ActionResult(ActionResultCode.MISSING_INPUT);
													else {
														environment.addOutput("The parameter was " + input);
														return new ActionResult(ActionResultCode.SUCCESS);
													}
												} else {
													environment.addOutput("Requesting input is not supported.");
													return new ActionResult(ActionResultCode.MISSING_FUNCTIONALITY);
												}
											}
										}

									})));

					// Context constructor for "samplecontextid"
					actionList.add(new ContextConstructorAction("SampleContextConstructor", this, "samplecontextid",
							3600000, ContextVisibility.PACKAGE, "build up a new context", new ActionMethod() {

								@Override
								public ActionResult run(Environment environment, Sentence sentence,
										Parameter[] parameter) {
									environment.addOutput("Context created.");
									return new ActionResult(ActionResultCode.CREATE_CONTEXT);
								}

							}));

					// Context depending on "samplecontextid"
					actionList.add(new ContextDependentAction("SampleContextAction", this, "samplecontextid",
							ContextVisibility.PACKAGE, "launch context action", new ActionMethod() {

								@Override
								public ActionResult run(Environment e, Sentence sentence, Parameter[] parameter) {
									e.addOutput("This is a yes/no question?");
									String req = e.requestInput("Question? Yes / No");
									Sentence sen = language.getSentence(req);
									String[] checks = new String[] { "yes", "no" };
									for (int i = 0; i < checks.length; i++) {
										for (Word w : sen.getWords()) {
											if (w.equals(language.getWord(checks[i]), false)) {
												switch (i) {
												case 0:
													e.addOutput("You answered yes.");
													return new ActionResult(ActionResultCode.SUCCESS);
												case 1:
													e.addOutput("You answered no.");
													return new ActionResult(ActionResultCode.SUCCESS);
												default:
													break;
												}
											}
										}
									}
									e.addOutput("I do not understand what you are meaning.");
									return new ActionResult(ActionResultCode.MISSING_INPUT);
								}

							}));

					// Context destroying "samplecontextid"
					actionList.add(new ContextDestructorAction("SampleContextDestructor", this, "samplecontextid",
							ContextVisibility.PACKAGE, "destroy context", new ActionMethod() {

								@Override
								public ActionResult run(Environment environment, Sentence sentence,
										Parameter[] parameter) {
									environment.addOutput("Context destroyed.");
									return new ActionResult(ActionResultCode.DESTROY_CONTEXT);
								}

							}));

					return actionList.toArray(new Action[0]);
				}
			};

			// add the Actions from the ActionPackage
			mySystem.getActionManager().add(sampleActionPackage.getActions(mySystem.getLanguage()));

			/*
			 * add Actions from .acp files in the
			 * mySystem.getPackageFilesDirectory() (usually the
			 * mySystem.getDataDirectory() + /packages)
			 */
			for (File f : mySystem.getPackageFilesDirectory().listFiles()) {
				if (f.getName().endsWith(".acp"))
					ActionPackageFromJarLoader.loadFromJar(f.getAbsolutePath(), mySystem);
			}

			// Establish a Server
			Server s = new Server(4964);
			s.addUser(new User("sample", "samplepassword"));
			s.addUser(new User("restrictedsample", "samplepassword", new String[] { "/restrictedpath" }));
			s.registerContext(new ServerContext("/request", mySystem, new SampleRequestHandler(), true));
			s.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}