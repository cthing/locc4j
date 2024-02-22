//
//  ViewController.m
//  HelloCpp
//
//  Created by Rico Zuniga on 16/02/2016.
//  Copyright © 2016 Sitepoint. All rights reserved.
//

#import "ViewController.h"
#import "Greeting.hpp"

@interface ViewController ()
{
    Greeting greeting;
    IBOutlet UIButton *helloButton;
}
@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)showGreeting {

    NSString* newTitle = [NSString stringWithCString:greeting.greet().c_str() encoding:[NSString defaultCStringEncoding]];

    [helloButton setTitle:newTitle forState:UIControlStateNormal];
}


@end
